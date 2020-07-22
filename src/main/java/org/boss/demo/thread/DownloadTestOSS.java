package org.boss.demo.thread;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DownloadFileRequest;
import com.aliyun.oss.model.DownloadFileResult;
import lombok.extern.slf4j.Slf4j;
import org.boss.demo.oss.OSSTools;

@Slf4j
public class DownloadTestOSS {

    private static OSS ossClient = null;

    public String download(String file, String key) {
        ossClient = new OSSClientBuilder().build(OSSTools.endPoint, OSSTools.accessKeyId, OSSTools.getAccessKeySecret);
        // 下载请求，10个任务并发下载，启动断点续传
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(OSSTools.bucketName, key);
        downloadFileRequest.setDownloadFile(file);
        downloadFileRequest.setPartSize(1*1024*1024);
        downloadFileRequest.setTaskNum(10);
        downloadFileRequest.setEnableCheckpoint(true);
        log.info("DownloadTestOSS download # begin ");
        // 下载文件
        DownloadFileResult downloadFileResult = null;
        try {
            downloadFileResult = ossClient.downloadFile(downloadFileRequest);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        downloadFileResult.getObjectMetadata();
        log.info("DownloadTestOSS download # finish ");

        // 关闭OSSClient
        ossClient.shutdown();

        return null;
    }
}
