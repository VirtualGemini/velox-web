package com.velox.framework.file.noop;

import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.common.provider.FileProviderConstants;
import com.velox.framework.file.spi.client.FileClientManager;
import com.velox.framework.file.spi.client.FileClientConfig;
import com.velox.framework.file.support.client.FileClientTypeRegistry;
import org.springframework.lang.Nullable;

import java.util.List;

public class DisabledFileClientManager implements FileClientManager {

    private final FileClientTypeRegistry typeRegistry;

    public DisabledFileClientManager(FileClientTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    @Override
    public FileClient getFileClient(String configId) {
        return new DisabledFileClient(configId != null ? configId : FileProviderConstants.DISABLED_CLIENT_ID);
    }

    @Override
    public <Config extends FileClientConfig> void createOrUpdateFileClient(String configId, Integer storage, Config config) {
        // Disabled mode keeps API available but never initializes real clients.
    }

    @Override
    @Nullable
    public Class<? extends FileClientConfig> getConfigClass(Integer storage) {
        return typeRegistry.require(storage).configClass();
    }

    @Override
    public List<Integer> getSupportedStorageTypes() {
        return typeRegistry.storageTypes();
    }
}
