package com.velox.framework.file.support.client.local;

import cn.hutool.core.io.FileUtil;
import com.velox.framework.file.spi.client.AbstractFileClient;

import java.io.File;

public class LocalFileClient extends AbstractFileClient<LocalFileClientConfig> {

    public LocalFileClient(String id, LocalFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
    }

    @Override
    protected String doUpload(byte[] content, String path, String type) {
        String filePath = getFilePath(path);
        FileUtil.writeBytes(content, filePath);
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    protected void doDelete(String path) {
        String filePath = getFilePath(path);
        FileUtil.del(filePath);
    }

    @Override
    protected byte[] doGetContent(String path) {
        String filePath = getFilePath(path);
        if (!FileUtil.exist(filePath)) {
            return null;
        }
        return FileUtil.readBytes(filePath);
    }

    private String getFilePath(String path) {
        return config.getBasePath() + File.separator + path;
    }
}
