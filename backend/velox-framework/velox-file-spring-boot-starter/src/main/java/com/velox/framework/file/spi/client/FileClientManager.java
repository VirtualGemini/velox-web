package com.velox.framework.file.spi.client;

import com.velox.framework.file.api.client.FileClientFactory;
import org.springframework.lang.Nullable;

import java.util.List;

public interface FileClientManager extends FileClientFactory {

    <Config extends FileClientConfig> void createOrUpdateFileClient(String configId, Integer storage, Config config);

    @Nullable
    Class<? extends FileClientConfig> getConfigClass(Integer storage);

    default List<Integer> getSupportedStorageTypes() {
        return List.of();
    }
}
