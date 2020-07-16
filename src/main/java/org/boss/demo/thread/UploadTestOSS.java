package org.boss.demo.thread;

import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;
import org.boss.demo.common_oss.OSSTools;
import org.boss.demo.service.imp.FileOSSServiceImpl;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UploadTestOSS {

    private static OSS ossClient = null;
    // 创建一个可重用固定数量的线程池
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public String upload(File file, String key){
        String endPoint = OSSTools.endPoint;
        String accessKeyId = OSSTools.accessKeyId;
        String accessKeySecret = OSSTools.getAccessKeySecret;
        String bucketName = OSSTools.bucketName;

        if (file == null){
            log.debug("TestOSS upload error : 文件" + file.getPath() + "为空");
            return null;
        }
        // 设置连接的参数
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        // 设置连接超时
        conf.setIdleConnectionTime(1000);
        // 初始化OSS连接
        ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret, conf);

        /**实现分片上传*/
        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, key);
        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        // 这里增加同步锁
        List<PartETag> partETags = Collections.synchronizedList(new ArrayList<>());
        log.info(" TestOSS upload # uploadId ： " + uploadId);

        // 分片上传
        try {
            /* 计算文件分片数量 */
            long partSize = 1*1024*1024L; // 1M
            long fileLength = file.length();
            int partCount = (int) (fileLength/partSize);
            if (fileLength % partSize != 0){
                partCount++;
            }
            // 对数量进行一个简单的判断
            if(partCount>10000){
                throw new RuntimeException("Total parts count should not exceed 10000");
            }else {
                log.debug("总块数：" + partCount);
            }

            /* 遍历分片 用线程上传 */
            log.debug("TestOSS 分片上传 # 开始" + file.getName());
            for(int i=0; i<partCount; i++){
                long startPos = i*partSize;
                // 判断是否为最后一块，最后一块的大小与其他的不一样
                long curPartSize=(i+1==partCount)?(fileLength-startPos):partSize;
                executorService.execute(new PartUploader(bucketName, file, startPos, curPartSize, i+1, uploadId, partETags, key));
            }
            // 等待所有分片上传结束, 关闭线程池
            executorService.shutdown();
            while (!executorService.isTerminated()){
                try {
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e){
                    log.error(e.getMessage());
                }
            }

            // 判断是否上传好了
            if (partETags.size() == partCount){
                System.out.println("TestOSS 分片上传 # 结束" + file.getName());
            } else {
                throw new IllegalStateException("文件上传不完整，文件部分上传失败");
            }

            /** 将上传好的文件通过partNumber进行排序 */
            Collections.sort(partETags, new Comparator<PartETag>() {
                @Override
                public int compare(PartETag o1, PartETag o2) {
                    return o1.getPartNumber()-o2.getPartNumber();
                }
            });

            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, key, uploadId, partETags);
            // 完成上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);

        } catch (OSSException e){
            log.error(e.getMessage());
        } catch (ClientException e){
            log.error(e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /* 线程 */
    private static class PartUploader implements Runnable {

        private String bucketName;
        private File localFile;
        private long startPos;
        private long partSize;
        private int partNumber;
        private String uploadId;
        private List<PartETag> partETags;
        private String key;

        public PartUploader(String bucketName, File localFile, long startPos,
                            long partSize, int partNumber, String uploadId,
                            List<PartETag> partETags, String key){
            this.bucketName = bucketName;
            this.localFile = localFile;
            this.startPos = startPos;
            this.partNumber = partNumber;
            this.uploadId = uploadId;
            this.partSize = partSize;
            this.partETags = partETags;
            this.key = key;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                // 分片上传配置
                inputStream = new FileInputStream(this.localFile);
                // 跳过已经上传的分片
                inputStream.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(key);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(inputStream);
                // 设置分片大小，除了最后一个
                uploadPartRequest.setPartSize(this.partSize);
                // 设置分片号，后面要排序的
                uploadPartRequest.setPartNumber(this.partNumber);

                // 上传分片
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                log.info("Part # " + this.partNumber + " done \n");
                synchronized (this.partETags) {
                    this.partETags.add(uploadPartResult.getPartETag());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
    }
}
