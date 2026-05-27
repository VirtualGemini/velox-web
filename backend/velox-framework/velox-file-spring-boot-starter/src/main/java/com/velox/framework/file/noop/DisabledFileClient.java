package com.velox.framework.file.noop;

import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.exception.FileClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisabledFileClient implements FileClient {

    private static final Logger log = LoggerFactory.getLogger(DisabledFileClient.class);

    private final String id;

    public DisabledFileClient(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        log.warn(FileCommonMessages.FILE_CLIENT_DISABLED_OPERATION, id, path);
        throw disabledException();
    }

    @Override
    public void delete(String path) {
        log.warn(FileCommonMessages.FILE_CLIENT_DISABLED_OPERATION, id, path);
        throw disabledException();
    }

    @Override
    public byte[] getContent(String path) {
        log.warn(FileCommonMessages.FILE_CLIENT_DISABLED_OPERATION, id, path);
        throw disabledException();
    }

    @Override
    public String presignPutUrl(String path) {
        log.warn(FileCommonMessages.FILE_CLIENT_DISABLED_OPERATION, id, path);
        throw disabledException();
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        log.warn(FileCommonMessages.FILE_CLIENT_DISABLED_OPERATION, id, url);
        throw disabledException();
    }

    private FileClientException disabledException() {
        return FileClientException.disabled();
    }
}
