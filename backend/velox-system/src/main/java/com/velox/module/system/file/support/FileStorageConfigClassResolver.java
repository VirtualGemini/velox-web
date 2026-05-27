package com.velox.module.system.file.support;

import com.velox.framework.file.common.storage.FileStorageCodes;
import com.velox.framework.file.spi.client.FileClientConfig;
import com.velox.framework.file.support.client.ftp.FtpFileClientConfig;
import com.velox.framework.file.support.client.local.LocalFileClientConfig;
import com.velox.framework.file.support.client.s3.S3FileClientConfig;
import com.velox.framework.file.support.client.sftp.SftpFileClientConfig;
import com.velox.module.system.file.provider.db.DbFileClientConfig;

public final class FileStorageConfigClassResolver {

    private FileStorageConfigClassResolver() {
    }

    public static Class<? extends FileClientConfig> resolve(Integer storage) {
        if (storage == null) {
            return null;
        }
        return switch (storage) {
            case FileStorageCodes.LOCAL -> LocalFileClientConfig.class;
            case FileStorageCodes.FTP -> FtpFileClientConfig.class;
            case FileStorageCodes.SFTP -> SftpFileClientConfig.class;
            case FileStorageCodes.S3 -> S3FileClientConfig.class;
            case 1 -> DbFileClientConfig.class;
            default -> null;
        };
    }
}
