package com.velox.framework.file.support.client.ftp;

import com.velox.framework.file.common.message.FileCommonValidationMessages;
import com.velox.framework.file.spi.client.FileClientConfig;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import org.hibernate.validator.constraints.URL;

import java.util.Arrays;

public class FtpFileClientConfig implements FileClientConfig {

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

    @NotEmpty(message = FileCommonValidationMessages.MODE_REQUIRED)
    private String mode;

    @AssertTrue(message = FileCommonValidationMessages.MODE_INVALID)
    public boolean isModeValid() {
        return mode != null && Arrays.stream(cn.hutool.extra.ftp.FtpMode.values())
                .anyMatch(item -> item.name().equals(mode));
    }

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
