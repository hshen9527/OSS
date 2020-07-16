package org.boss.demo.common_oss;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.extern.slf4j.Slf4j;

/**
 * 存储一些OSS的配置信息
 * */
@Slf4j
public class OSSTools {

    // endPoint是访问OSS的域名（外网）
    public static final String endPoint = "http://oss-cn-beijing.aliyuncs.com";

    // accessKeyId和accessKeySecret是OSS的访问密钥
    public static final String accessKeyId = "LTAI4G1xyJLKkYL69HtZTtnM";
    public static final String getAccessKeySecret = "g2dG9ya1fgUUbv7q3sDyzBoTRqUBJ6";

    // Bucket用来存储Object的存储空间
    public static final String bucketName = "wlxtest0";

    // Object是OSS存储的基本单元，成为OSS的对象，也被称为OSS的文件
    public static final String objPath = "BossProject/project_20200715/";

    /**
     * 已经默认了存储位置，用户只用输入文件名称就可以初始化Object
     * @param fileName 文件名称
     * @return 文件目录
     */
    public static String initObject(String fileName){
        return objPath+fileName;
    }

    /**
     * 检查Bucket是否存在，并初始化OSS连接对象
     * @param ossClient OSS连接对象
     */
    public static void init(OSS ossClient){
        try {
            // 判断Bucket是否存在
            // 存在则打印一下结果，不存在则根据名称创建一个Bucket
            if (ossClient.doesBucketExist(bucketName)){
                log.debug("OOSTools---已经创建了Bucket：" + bucketName);
            } else {
                ossClient.createBucket(bucketName);
                log.debug("OOSTools---Bucket不存在，将为你创建Bucket：" + bucketName);
            }
        }catch (OSSException e){
            e.printStackTrace();
        }catch (ClientException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
        }
    };
}
