package com.velox.module.system.file.provider.db;

import com.velox.framework.file.common.message.FileCommonValidationMessages;
import com.velox.framework.file.spi.client.FileClientConfig;
import org.hibernate.validator.constraints.URL;

public class DbFileClientConfig implements FileClientConfig {

    @URL(message = FileCommonValidationMessages.DOMAIN_INVALID)
    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
