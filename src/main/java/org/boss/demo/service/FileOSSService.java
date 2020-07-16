package org.boss.demo.service;

import java.io.File;

public interface FileOSSService {

    boolean upload(File file, String objName);

    boolean download(File file, String objName);

    boolean delete(String objName);

    boolean uploadDirsToOss(File sourceFile, String objName);

}
