package com.velox.framework.file.spi.client;

import com.velox.framework.file.common.error.FileErrorCode;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.exception.FileClientException;

public record FileClientTypeRegistration(
        Integer storage,
        Class<? extends FileClientConfig> configClass,
        FileClientCreator creator,
        boolean builtIn
) {

    public FileClientTypeRegistration(
            Integer storage,
            Class<? extends FileClientConfig> configClass,
            FileClientCreator creator
    ) {
        this(storage, configClass, creator, false);
    }

    public FileClientTypeRegistration {
        if (storage == null) {
            throw new FileClientException(FileErrorCode.STORAGE_REQUIRED, FileCommonMessages.FILE_STORAGE_REQUIRED);
        }
        if (configClass == null) {
            throw new FileClientException(FileErrorCode.CLIENT_CONFIG_CLASS_REQUIRED,
                    FileCommonMessages.FILE_CLIENT_CONFIG_CLASS_REQUIRED);
        }
        if (creator == null) {
            throw new FileClientException(FileErrorCode.CLIENT_CREATOR_REQUIRED,
                    FileCommonMessages.FILE_CLIENT_CREATOR_REQUIRED);
        }
    }

    public static FileClientTypeRegistration builtIn(
            Integer storage,
            Class<? extends FileClientConfig> configClass,
            FileClientCreator creator
    ) {
        return new FileClientTypeRegistration(storage, configClass, creator, true);
    }
}
