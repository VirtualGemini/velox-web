package com.velox.framework.file.common.provider;

import java.time.Duration;

public final class FileProviderConstants {

    public static final Duration S3_DEFAULT_EXPIRATION = Duration.ofHours(24);
    public static final long FTP_CONNECTION_TIMEOUT_MILLIS = 3000L;
    public static final long FTP_SO_TIMEOUT_MILLIS = 10000L;
    public static final long SFTP_CONNECTION_TIMEOUT_MILLIS = 3000L;
    public static final long SFTP_SO_TIMEOUT_MILLIS = 10000L;
    public static final String TEMP_FILE_PREFIX = "upload";
    public static final String TEMP_FILE_SUFFIX = ".tmp";
    public static final String DEFAULT_AWS_REGION = "us-east-1";
    public static final String HTTPS_PREFIX = "https://";
    public static final String S3_HOST_PREFIX = "s3.";
    public static final String S3_ACCELERATE_REGION = "accelerate";
    public static final String OSS_HOST_PREFIX = "oss-";
    public static final String COS_HOST_PREFIX = "cos.";
    public static final String AWS_HOST_SUFFIX = ".amazonaws.com";
    public static final String QINIU_ENDPOINT = "qiniucs.com";
    public static final String ALIYUN_ENDPOINT = "aliyuncs.com";
    public static final String TENCENT_ENDPOINT = "myqcloud.com";
    public static final String VOLCES_ENDPOINT = "volces.com";
    public static final String SFTP_HOST_KEY_CONFIG = "server_host_key";
    public static final String SFTP_DSS_SUFFIX = ",ssh-dss";
    public static final String DISABLED_CLIENT_ID = "disabled";

    private FileProviderConstants() {
    }
}
