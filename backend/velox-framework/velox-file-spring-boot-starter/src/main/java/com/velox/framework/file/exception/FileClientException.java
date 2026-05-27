package com.velox.framework.file.exception;

import com.velox.framework.file.common.error.FileErrorCode;
import com.velox.framework.file.common.message.FileCommonMessages;

public class FileClientException extends VeloxFileException {

    private final String code;

    public FileClientException(FileErrorCode code, String message) {
        super(message);
        this.code = code.code();
    }

    public FileClientException(FileErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code.code();
    }

    public String getCode() {
        return code;
    }

    public static FileClientException disabled() {
        return new FileClientException(FileErrorCode.DISABLED, FileCommonMessages.FILE_CAPABILITY_DISABLED);
    }

    public static FileClientException clientNotFound(String configId) {
        return new FileClientException(FileErrorCode.CLIENT_NOT_FOUND,
                FileCommonMessages.FILE_CLIENT_NOT_FOUND.formatted(configId));
    }

    public static FileClientException unsupportedStorage(Integer storage) {
        return new FileClientException(FileErrorCode.UNSUPPORTED_STORAGE,
                FileCommonMessages.FILE_STORAGE_UNSUPPORTED.formatted(storage));
    }

    public static FileClientException operationNotSupported(String operation) {
        return new FileClientException(FileErrorCode.OPERATION_NOT_SUPPORTED,
                FileCommonMessages.FILE_OPERATION_NOT_SUPPORTED.formatted(operation));
    }

    public static FileClientException operationFailed(String operation, String configId, Throwable cause) {
        return new FileClientException(FileErrorCode.OPERATION_FAILED,
                FileCommonMessages.FILE_OPERATION_FAILED.formatted(operation, configId), cause);
    }

    public static FileClientException registrationConflict(Integer storage) {
        return new FileClientException(FileErrorCode.REGISTRATION_CONFLICT,
                FileCommonMessages.FILE_STORAGE_REGISTRATION_CONFLICT.formatted(storage));
    }

    public static FileClientException temporaryFileCreateFailed(Throwable cause) {
        return new FileClientException(FileErrorCode.TEMP_FILE_CREATE_FAILED,
                FileCommonMessages.FILE_TEMP_FILE_CREATE_FAILED, cause);
    }
}
