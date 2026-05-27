package com.velox.framework.file.spi.client;

import com.velox.framework.file.api.client.FileClient;

public interface ManagedFileClient<Config extends FileClientConfig> extends FileClient {

    void init();

    void refresh(Config config);
}
