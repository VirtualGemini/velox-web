package com.velox.framework.file.core.client;

import cn.hutool.core.util.StrUtil;
import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.common.error.FileErrorCode;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.exception.FileClientException;
import com.velox.framework.file.spi.client.FileClientManager;
import com.velox.framework.file.spi.client.FileClientConfig;
import com.velox.framework.file.spi.client.ManagedFileClient;
import com.velox.framework.file.spi.client.FileClientTypeRegistration;
import com.velox.framework.file.support.client.FileClientTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultFileClientManager implements FileClientManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultFileClientManager.class);

    private final ConcurrentMap<String, FileClient> clients = new ConcurrentHashMap<>();
    private final FileClientTypeRegistry typeRegistry;

    public DefaultFileClientManager(FileClientTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    @Override
    @Nullable
    public FileClient getFileClient(String configId) {
        return clients.get(configId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends FileClientConfig> void createOrUpdateFileClient(String configId, Integer storage, Config config) {
        requireConfigId(configId);
        FileClient candidate = createFileClient(configId, storage, config);
        clients.compute(configId, (id, existing) -> refreshOrReplace(existing, candidate, config));
    }

    @Override
    public Class<? extends FileClientConfig> getConfigClass(Integer storage) {
        FileClientTypeRegistration registration = typeRegistry.get(storage);
        return registration != null ? registration.configClass() : null;
    }

    @Override
    public java.util.List<Integer> getSupportedStorageTypes() {
        return typeRegistry.storageTypes();
    }

    @SuppressWarnings("unchecked")
    private <Config extends FileClientConfig> FileClient createFileClient(
            String configId, Integer storage, Config config) {
        FileClientTypeRegistration registration = typeRegistry.require(storage);
        return registration.creator().create(configId, config);
    }

    @SuppressWarnings("unchecked")
    private <Config extends FileClientConfig> FileClient refreshOrReplace(
            FileClient existing,
            FileClient candidate,
            Config config) {
        if (existing instanceof ManagedFileClient<?> managedExisting
                && candidate instanceof ManagedFileClient<?>
                && existing.getClass().equals(candidate.getClass())) {
            ((ManagedFileClient<Config>) managedExisting).refresh(config);
            return existing;
        }
        if (candidate instanceof ManagedFileClient<?> managedCandidate) {
            ((ManagedFileClient<Config>) managedCandidate).init();
        }
        log.info(FileCommonMessages.FILE_CLIENT_INITIALIZED, candidate.getId(), candidate.getClass().getSimpleName());
        return candidate;
    }

    private void requireConfigId(String configId) {
        if (StrUtil.isBlank(configId)) {
            throw new FileClientException(FileErrorCode.CLIENT_ID_REQUIRED, FileCommonMessages.FILE_CLIENT_ID_REQUIRED);
        }
    }
}
