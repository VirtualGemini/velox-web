package com.velox.framework.file.support.client.sftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.common.provider.FileProviderConstants;
import com.velox.framework.file.exception.FileClientException;
import com.velox.framework.file.spi.client.AbstractFileClient;
import com.jcraft.jsch.JSch;

import java.io.File;

public class SftpFileClient extends AbstractFileClient<SftpFileClientConfig> {

    static {
        JSch.setConfig(
                FileProviderConstants.SFTP_HOST_KEY_CONFIG,
                JSch.getConfig(FileProviderConstants.SFTP_HOST_KEY_CONFIG) + FileProviderConstants.SFTP_DSS_SUFFIX
        );
    }

    private Sftp sftp;

    public SftpFileClient(String id, SftpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        FtpConfig ftpConfig = new FtpConfig(config.getHost(), config.getPort(), config.getUsername(), config.getPassword(),
                CharsetUtil.CHARSET_UTF_8, null, null);
        ftpConfig.setConnectionTimeout(FileProviderConstants.SFTP_CONNECTION_TIMEOUT_MILLIS);
        ftpConfig.setSoTimeout(FileProviderConstants.SFTP_SO_TIMEOUT_MILLIS);
        this.sftp = new Sftp(ftpConfig);
    }

    @Override
    protected String doUpload(byte[] content, String path, String type) {
        String filePath = getFilePath(path);
        String fileName = FileUtil.getName(filePath);
        String dir = StrUtil.removeSuffix(filePath, fileName);
        File file = createTempFile(content);
        try {
            reconnectIfTimeout();
            sftp.mkDirs(dir);
            boolean success = sftp.upload(filePath, file);
            if (!success) {
                throw new JschRuntimeException(StrUtil.format(FileCommonMessages.FILE_SFTP_UPLOAD_FAILED, filePath));
            }
            return super.formatFileUrl(config.getDomain(), path);
        } finally {
            FileUtil.del(file);
        }
    }

    @Override
    protected void doDelete(String path) {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        sftp.delFile(filePath);
    }

    @Override
    protected byte[] doGetContent(String path) {
        String filePath = getFilePath(path);
        File destFile = createTempFile();
        try {
            reconnectIfTimeout();
            sftp.download(filePath, destFile);
            return FileUtil.readBytes(destFile);
        } finally {
            FileUtil.del(destFile);
        }
    }

    private String getFilePath(String path) {
        return config.getBasePath() + StrUtil.SLASH + path;
    }

    private synchronized void reconnectIfTimeout() {
        sftp.reconnectIfTimeout();
    }

    private static File createTempFile() {
        try {
            return File.createTempFile(FileProviderConstants.TEMP_FILE_PREFIX, FileProviderConstants.TEMP_FILE_SUFFIX);
        } catch (java.io.IOException e) {
            throw FileClientException.temporaryFileCreateFailed(e);
        }
    }

    private static File createTempFile(byte[] content) {
        try {
            File tempFile = File.createTempFile(FileProviderConstants.TEMP_FILE_PREFIX, FileProviderConstants.TEMP_FILE_SUFFIX);
            FileUtil.writeBytes(content, tempFile);
            return tempFile;
        } catch (java.io.IOException e) {
            throw FileClientException.temporaryFileCreateFailed(e);
        }
    }
}
