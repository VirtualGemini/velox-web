package com.velox.framework.file.support.client.local;

import com.velox.framework.file.common.message.FileCommonValidationMessages;
import com.velox.framework.file.spi.client.FileClientConfig;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotEmpty;

public class LocalFileClientConfig implements FileClientConfig {

    @NotEmpty(message = FileCommonValidationMessages.BASE_PATH_REQUIRED)
    private String basePath;

    @NotEmpty(message = FileCommonValidationMessages.DOMAIN_REQUIRED)
    @URL(message = FileCommonValidationMessages.DOMAIN_INVALID)
    private String domain;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
