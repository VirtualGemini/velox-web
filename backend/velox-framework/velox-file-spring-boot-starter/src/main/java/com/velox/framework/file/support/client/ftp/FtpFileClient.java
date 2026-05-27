package com.velox.framework.file.support.client.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ftp.FtpException;
import cn.hutool.extra.ftp.FtpMode;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.common.provider.FileProviderConstants;
import com.velox.framework.file.spi.client.AbstractFileClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class FtpFileClient extends AbstractFileClient<FtpFileClientConfig> {

    private Ftp ftp;

    public FtpFileClient(String id, FtpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        FtpConfig ftpConfig = new FtpConfig(config.getHost(), config.getPort(), config.getUsername(), config.getPassword(),
                CharsetUtil.CHARSET_UTF_8, null, null);
        ftpConfig.setConnectionTimeout(FileProviderConstants.FTP_CONNECTION_TIMEOUT_MILLIS);
        ftpConfig.setSoTimeout(FileProviderConstants.FTP_SO_TIMEOUT_MILLIS);
        this.ftp = new Ftp(ftpConfig, FtpMode.valueOf(config.getMode()));
    }

    @Override
    protected String doUpload(byte[] content, String path, String type) {
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        reconnectIfTimeout();
        boolean success = ftp.upload(dir, fileName, new ByteArrayInputStream(content));
        if (!success) {
            throw new FtpException(StrUtil.format(FileCommonMessages.FILE_FTP_UPLOAD_FAILED, filePath));
        }
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    protected void doDelete(String path) {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        ftp.delFile(filePath);
    }

    @Override
    protected byte[] doGetContent(String path) {
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        reconnectIfTimeout();
        ftp.download(dir, fileName, out);
        return out.toByteArray();
    }

    private String getFilePath(String path) {
        return config.getBasePath() + StrUtil.SLASH + path;
    }

    private synchronized void reconnectIfTimeout() {
        ftp.reconnectIfTimeout();
    }
}
