package com.velox.framework.file.api.client;

import com.velox.framework.file.common.type.FileClientOperationType;
import com.velox.framework.file.exception.FileClientException;

public interface FileClient {

    String getId();

    String upload(byte[] content, String path, String type);

    void delete(String path);

    byte[] getContent(String path);

    default String presignPutUrl(String path) {
        throw FileClientException.operationNotSupported(FileClientOperationType.PRESIGN_PUT_URL);
    }

    default String presignGetUrl(String url, Integer expirationSeconds) {
        throw FileClientException.operationNotSupported(FileClientOperationType.PRESIGN_GET_URL);
    }
}
