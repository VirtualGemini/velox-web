package com.velox.framework.file.support.client.s3;

import cn.hutool.core.util.StrUtil;
import com.velox.framework.file.common.message.FileCommonValidationMessages;
import com.velox.framework.file.common.provider.FileProviderConstants;
import com.velox.framework.file.spi.client.FileClientConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class S3FileClientConfig implements FileClientConfig {

    public static final String ENDPOINT_QINIU = FileProviderConstants.QINIU_ENDPOINT;
    public static final String ENDPOINT_ALIYUN = FileProviderConstants.ALIYUN_ENDPOINT;
    public static final String ENDPOINT_TENCENT = FileProviderConstants.TENCENT_ENDPOINT;
    public static final String ENDPOINT_VOLCES = FileProviderConstants.VOLCES_ENDPOINT;

    @NotNull(message = FileCommonValidationMessages.ENDPOINT_REQUIRED)
    private String endpoint;

    @URL(message = FileCommonValidationMessages.DOMAIN_INVALID)
    private String domain;

    @NotNull(message = FileCommonValidationMessages.BUCKET_REQUIRED)
    private String bucket;

    private String basePath;

    @NotNull(message = FileCommonValidationMessages.ACCESS_KEY_REQUIRED)
    private String accessKey;

    @NotNull(message = FileCommonValidationMessages.ACCESS_SECRET_REQUIRED)
    private String accessSecret;

    private String sessionToken;

    @NotNull(message = FileCommonValidationMessages.ENABLE_PATH_STYLE_ACCESS_REQUIRED)
    private Boolean enablePathStyleAccess;

    @NotNull(message = FileCommonValidationMessages.ENABLE_PUBLIC_ACCESS_REQUIRED)
    private Boolean enablePublicAccess;

    private String region;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Boolean getEnablePathStyleAccess() {
        return enablePathStyleAccess;
    }

    public void setEnablePathStyleAccess(Boolean enablePathStyleAccess) {
        this.enablePathStyleAccess = enablePathStyleAccess;
    }

    public Boolean getEnablePublicAccess() {
        return enablePublicAccess;
    }

    public void setEnablePublicAccess(Boolean enablePublicAccess) {
        this.enablePublicAccess = enablePublicAccess;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @SuppressWarnings("RedundantIfStatement")
    @AssertTrue(message = FileCommonValidationMessages.QINIU_DOMAIN_REQUIRED)
    @JsonIgnore
    public boolean isDomainValid() {
        if (StrUtil.contains(endpoint, ENDPOINT_QINIU) && StrUtil.isEmpty(domain)) {
            return false;
        }
        return true;
    }
}
