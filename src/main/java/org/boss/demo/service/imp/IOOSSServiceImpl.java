package org.boss.demo.service.imp;

import com.aliyun.oss.OSS;
import org.boss.demo.service.OSSService;

import java.io.File;

public class IOOSSServiceImpl implements OSSService {

    @Override
    public boolean upload(File file, String objName) {
        return false;
    }

    @Override
    public boolean download(File file, String objName) {
        return false;
    }

    @Override
    public boolean delete(String objName) {
        return false;
    }
}
