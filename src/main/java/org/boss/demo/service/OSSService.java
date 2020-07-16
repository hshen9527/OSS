package org.boss.demo.service;

import java.io.File;

public interface OSSService {

    boolean upload(File file, String objName);

    boolean download(File file, String objName);

    boolean delete(String objName);

}
