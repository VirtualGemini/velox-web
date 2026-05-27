package com.velox.framework.file.spi.client;

import com.velox.framework.file.api.client.FileClient;

@FunctionalInterface
public interface FileClientCreator {

    FileClient create(String configId, FileClientConfig config);
}
