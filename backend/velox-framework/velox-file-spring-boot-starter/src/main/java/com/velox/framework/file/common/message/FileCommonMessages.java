package com.velox.framework.file.common.message;

public final class FileCommonMessages {

    public static final String FILE_CAPABILITY_DISABLED =
            "Velox file capability is disabled. Please enable velox.file.enabled=true.";
    public static final String FILE_CLIENT_NOT_FOUND = "File client not found for configId=%s";
    public static final String FILE_STORAGE_UNSUPPORTED = "Unsupported file storage type: %s";
    public static final String FILE_OPERATION_NOT_SUPPORTED = "File operation is not supported: %s";
    public static final String FILE_OPERATION_FAILED = "File client operation failed: %s, configId=%s";
    public static final String FILE_STORAGE_REGISTRATION_CONFLICT =
            "Duplicate file storage registration for storage=%s";
    public static final String FILE_CLIENT_REGISTRATION_OVERRIDDEN =
            "Overrode built-in file client registration for storage={} from {} to {}";
    public static final String FILE_CLIENT_REGISTRATION_IGNORED =
            "Ignored built-in file client registration for storage={} because custom registration {} is already active";
    public static final String FILE_CLIENT_ID_REQUIRED = "File client id must not be blank";
    public static final String FILE_CLIENT_CONFIG_REQUIRED = "File client config must not be null";
    public static final String FILE_CLIENT_CONFIG_CLASS_REQUIRED = "File client config class must not be null";
    public static final String FILE_CLIENT_CREATOR_REQUIRED = "File client creator must not be null";
    public static final String FILE_STORAGE_REQUIRED = "File storage type must not be null";
    public static final String FILE_TEMP_FILE_CREATE_FAILED = "Failed to create temporary file for file transfer";
    public static final String FILE_CONFIG_BASE_PATH_REQUIRED = "basePath must not be blank";
    public static final String FILE_CONFIG_DOMAIN_REQUIRED = "domain must not be blank";
    public static final String FILE_CONFIG_DOMAIN_INVALID = "domain must be a valid URL";
    public static final String FILE_CONFIG_HOST_REQUIRED = "host must not be blank";
    public static final String FILE_CONFIG_PORT_REQUIRED = "port must not be null";
    public static final String FILE_CONFIG_USERNAME_REQUIRED = "username must not be blank";
    public static final String FILE_CONFIG_PASSWORD_REQUIRED = "password must not be blank";
    public static final String FILE_CONFIG_MODE_REQUIRED = "mode must not be blank";
    public static final String FILE_CONFIG_MODE_INVALID = "mode must be one of the declared FTP modes";
    public static final String FILE_CONFIG_ENDPOINT_REQUIRED = "endpoint must not be null";
    public static final String FILE_CONFIG_BUCKET_REQUIRED = "bucket must not be null";
    public static final String FILE_CONFIG_ACCESS_KEY_REQUIRED = "accessKey must not be null";
    public static final String FILE_CONFIG_ACCESS_SECRET_REQUIRED = "accessSecret must not be null";
    public static final String FILE_CONFIG_ENABLE_PATH_STYLE_ACCESS_REQUIRED = "enablePathStyleAccess must not be null";
    public static final String FILE_CONFIG_ENABLE_PUBLIC_ACCESS_REQUIRED = "enablePublicAccess must not be null";
    public static final String FILE_CONFIG_QINIU_DOMAIN_REQUIRED = "domain must not be blank when using qiniucs.com endpoint";
    public static final String FILE_EXTENSION_RESOLVE_FAILED = "Failed to resolve file extension for mimeType=%s";
    public static final String FILE_FTP_UPLOAD_FAILED = "Failed to upload file to FTP path=%s";
    public static final String FILE_SFTP_UPLOAD_FAILED = "Failed to upload file to SFTP path=%s";
    public static final String FILE_CLIENT_INIT_COMPLETED = "Initialized file client config={}";
    public static final String FILE_CLIENT_REFRESH_TRIGGERED = "Refreshing file client because config changed: {}";
    public static final String FILE_CLIENT_DISABLED_OPERATION = "File capability disabled for configId={}, target={}";
    public static final String FILE_CLIENT_INITIALIZED = "Initialized file client id={} type={}";

    private FileCommonMessages() {
    }
}
