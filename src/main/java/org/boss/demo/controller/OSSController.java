package org.boss.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.boss.demo.service.FileOSSService;
import org.boss.demo.thread.DownloadTestOSS;
import org.boss.demo.thread.UploadTestOSS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
@RequestMapping("/OSS")
@Slf4j
public class OSSController {

    @Autowired
    private FileOSSService service;

    @ResponseBody
    @PostMapping("/upload")
    public void upload(){
        File file = new File("E:\\TestPATH\\test01\\test01.java");
        if (file.exists()){
            service.upload(file, file.getName());
        } else {
            System.out.println("文件不存在" + file.getPath());
//            log.debug("文件不存在" + file.getPath());
        }
    }

    @ResponseBody
    @PostMapping("/uploadFromDir")
    public void uploadFromDir(){
        File file = new File("E:\\TestPATH");
        if (file.exists()){
            if(file.isDirectory()){
                service.uploadDirsToOss(file, file.getName());
            }
        } else {
            System.out.println("文件不存在" + file.getPath());
//            log.debug("文件不存在" + file.getPath());
        }
    }

    @ResponseBody
    @PostMapping("/download")
    public void download(){
        File file = new File("E:\\TestPATH\\test02\\test.txt");
        if (file.exists()){
            service.download(file, "test01.java");
        } else {
            System.out.println("文件不存在" + file.getPath());
//            log.debug("文件不存在" + file.getPath());
        }
    }

    @ResponseBody
    @PostMapping("/delete")
    public void delete(){
        service.delete("test.java");
    }

    @ResponseBody
    @PostMapping("/uploadBigPacket")
    public void uploadBigPacket(){
        UploadTestOSS testOSS = new UploadTestOSS();
        File file = new File("E:\\TestPATH\\test.zip");
        if (file.exists()){
            testOSS.upload(file, file.getName());
        } else {
            System.out.println("文件不存在" + file.getPath());
//            log.debug("文件不存在" + file.getPath());
        }
    }

    @ResponseBody
    @PostMapping("/downloadBigPacket")
    public void downloadBigPacket(){
        DownloadTestOSS testOSS = new DownloadTestOSS();
        testOSS.download("E:\\TestPATH\\test03\\test03.zip", "test.zip");

    }
}
