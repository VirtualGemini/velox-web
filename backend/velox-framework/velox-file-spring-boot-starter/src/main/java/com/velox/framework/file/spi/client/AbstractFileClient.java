package com.velox.framework.file.spi.client;

import cn.hutool.core.util.StrUtil;
import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.common.error.FileErrorCode;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.common.type.FileClientOperationType;
import com.velox.framework.file.common.web.FileWebConstants;
import com.velox.framework.file.exception.FileClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

public abstract class AbstractFileClient<Config extends FileClientConfig> implements ManagedFileClient<Config> {

    private static final Logger log = LoggerFactory.getLogger(AbstractFileClient.class);

    private final String id;
    protected Config config;
    private Config originalConfig;

    public AbstractFileClient(String id, Config config) {
        this.id = requireId(id);
        this.config = requireConfig(config);
        this.originalConfig = config;
    }

    public final void init() {
        doInit();
        log.debug(FileCommonMessages.FILE_CLIENT_INIT_COMPLETED, config);
    }

    protected abstract void doInit();

    public final void refresh(Config config) {
        if (config.equals(this.originalConfig)) {
            return;
        }
        log.info(FileCommonMessages.FILE_CLIENT_REFRESH_TRIGGERED, config);
        this.config = config;
        this.originalConfig = config;
        this.init();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public final String upload(byte[] content, String path, String type) {
        return execute(FileClientOperationType.UPLOAD, () -> doUpload(content, path, type));
    }

    @Override
    public final void delete(String path) {
        execute(FileClientOperationType.DELETE, () -> {
            doDelete(path);
            return null;
        });
    }

    @Override
    public final byte[] getContent(String path) {
        return execute(FileClientOperationType.GET_CONTENT, () -> doGetContent(path));
    }

    @Override
    public String presignPutUrl(String path) {
        return execute(FileClientOperationType.PRESIGN_PUT_URL, () -> doPresignPutUrl(path));
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        return execute(FileClientOperationType.PRESIGN_GET_URL, () -> doPresignGetUrl(url, expirationSeconds));
    }

    protected abstract String doUpload(byte[] content, String path, String type) throws Exception;

    protected abstract void doDelete(String path) throws Exception;

    protected abstract byte[] doGetContent(String path) throws Exception;

    protected String doPresignPutUrl(String path) {
        throw FileClientException.operationNotSupported(FileClientOperationType.PRESIGN_PUT_URL);
    }

    protected String doPresignGetUrl(String url, Integer expirationSeconds) {
        throw FileClientException.operationNotSupported(FileClientOperationType.PRESIGN_GET_URL);
    }

    protected String formatFileUrl(String domain, String path) {
        String relativePath = StrUtil.format(FileWebConstants.API_FILE_GET_PATH, getId(), encodePath(path));
        if (StrUtil.isBlank(domain)) {
            return relativePath;
        }
        String normalizedDomain = StrUtil.removeSuffix(domain, FileWebConstants.URL_PATH_SEPARATOR);
        return normalizedDomain + relativePath;
    }

    private String encodePath(String path) {
        return StrUtil.splitTrim(path, StrUtil.SLASH).stream()
                .map(item -> UriUtils.encodePathSegment(item, StandardCharsets.UTF_8))
                .reduce((left, right) -> left + StrUtil.SLASH + right)
                .orElse(FileWebConstants.EMPTY);
    }

    private <T> T execute(String operation, FileClientOperation<T> operationInvoker) {
        try {
            return operationInvoker.invoke();
        } catch (FileClientException exception) {
            throw exception;
        } catch (Exception exception) {
            throw FileClientException.operationFailed(operation, getId(), exception);
        }
    }

    private static String requireId(String id) {
        if (StrUtil.isBlank(id)) {
            throw new FileClientException(FileErrorCode.CLIENT_ID_REQUIRED, FileCommonMessages.FILE_CLIENT_ID_REQUIRED);
        }
        return id;
    }

    private static <Config extends FileClientConfig> Config requireConfig(Config config) {
        if (config == null) {
            throw new FileClientException(FileErrorCode.CLIENT_CONFIG_REQUIRED, FileCommonMessages.FILE_CLIENT_CONFIG_REQUIRED);
        }
        return config;
    }

    @FunctionalInterface
    private interface FileClientOperation<T> {
        T invoke() throws Exception;
    }
}
