package org.boss.demo.service.imp;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import lombok.extern.slf4j.Slf4j;
import org.boss.demo.oss.OSSTools;
import org.boss.demo.service.FileOSSService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class FileOSSServiceImpl implements FileOSSService {

    /**
     * 单个文件上传
     * @param file 上传的文件
     * @param objName oss存储路径
     * @return
     */
    @Override
    public boolean upload(File file, String objName) {
        OSS ossClient = new OSSClientBuilder().build(OSSTools.endPoint, OSSTools.accessKeyId, OSSTools.getAccessKeySecret);
        OSSTools.init(ossClient);
        ossClient.putObject(OSSTools.bucketName, OSSTools.objPath+objName, file);
        ossClient.shutdown();
        log.debug("增加Object ：" + objName + "成功");
        return true;
    }

    /**
     * 单个文件下载
     * @param file 文件的下载位置。如果指定的文件存在就会覆盖，不存在则会新建
     * @param objName oss存放位置
     * @return
     */
    @Override
    public boolean download(File file, String objName) {
        OSS ossClient = new OSSClientBuilder().build(OSSTools.endPoint, OSSTools.accessKeyId, OSSTools.getAccessKeySecret);
        OSSTools.init(ossClient);
//        // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
//        String key = OSSTools.objPath + objName;
//        ossClient.getObject(new GetObjectRequest(OSSTools.bucketName, key), new File("E:\\TestPATH\\test02"));
//        ossClient.shutdown();

        // 下载OSS文件流
        try (FileOutputStream fos = new FileOutputStream(file)) {
            OSSObject object = ossClient.getObject(OSSTools.bucketName, OSSTools.objPath+objName);

            InputStream is = object.getObjectContent();
            byte[] b = new byte[1024];
            int length;
            while ((length = is.read(b)) != -1) {
                fos.write(b, 0, length);
            }
            is.close();
        } catch (IOException e){
            e.printStackTrace();
        }finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }

        return true;
    }

    /**
     * 删除单个Object
     * @param objName object 存放位置
     * @return
     */
    @Override
    public boolean delete(String objName) {
        OSS ossClient = new OSSClientBuilder().build(OSSTools.endPoint, OSSTools.accessKeyId, OSSTools.getAccessKeySecret);
        OSSTools.init(ossClient);
        ossClient.deleteObject(OSSTools.bucketName, OSSTools.objPath+objName);
        ossClient.shutdown();
        log.debug("删除Object ：" + objName + "成功");
        return true;
    }


    /**
     * 递归文件目录上传
     * @param sourceFile 本地文件目录
     * @param objName oss存储路径
     * @return
     */
    @Override
    public boolean uploadDirsToOss(File sourceFile, String objName){
        if (!sourceFile.exists()){
            log.debug("FileOSSService---上传文件目录不存:"+sourceFile.getPath());
            return false;
        }
        File[] files=sourceFile.listFiles();
        if (files!=null){
            for (File file:files){
                if (file.isFile()){
                    upload(file,file.getName());
                }else if (file.isDirectory()){
                    uploadDirsToOss(file,file.getName());
                }
            }
        }
        return true;
    }

}
