package com.velox.framework.file.api.client;

import com.velox.framework.file.exception.FileClientException;
import org.springframework.lang.Nullable;

public interface FileClientFactory {

    @Nullable
    FileClient getFileClient(String configId);

    default FileClient requireFileClient(String configId) {
        FileClient client = getFileClient(configId);
        if (client == null) {
            throw FileClientException.clientNotFound(configId);
        }
        return client;
    }
}
