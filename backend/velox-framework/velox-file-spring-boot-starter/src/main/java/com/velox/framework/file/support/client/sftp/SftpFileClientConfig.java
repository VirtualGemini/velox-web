package com.velox.framework.file.support.client.sftp;

import com.velox.framework.file.common.message.FileCommonValidationMessages;
import com.velox.framework.file.spi.client.FileClientConfig;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public class SftpFileClientConfig implements FileClientConfig {

    @NotEmpty(message = FileCommonValidationMessages.BASE_PATH_REQUIRED)
    private String basePath;

    @URL(message = FileCommonValidationMessages.DOMAIN_INVALID)
    private String domain;

    @NotEmpty(message = FileCommonValidationMessages.HOST_REQUIRED)
    private String host;

    @NotNull(message = FileCommonValidationMessages.PORT_REQUIRED)
    private Integer port;

    @NotEmpty(message = FileCommonValidationMessages.USERNAME_REQUIRED)
    private String username;

    @NotEmpty(message = FileCommonValidationMessages.PASSWORD_REQUIRED)
    private String password;

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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
