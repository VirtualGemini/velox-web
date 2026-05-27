package com.velox.module.system.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "openapi.system.auth.login_command.schema")
public class LoginCommand {

    @Schema(description = "openapi.system.auth.login_command.username", example = "admin")
    @JsonAlias({"userName", "account"})
    @NotBlank(message = "{validation.system.auth.login.username.not_blank}")
    @Size(min = 3, max = 64, message = "{validation.system.auth.login.username.size}")
    private String username;

    @Schema(description = "openapi.system.auth.login_command.password", example = "123456")
    @NotBlank(message = "{validation.system.auth.login.password.not_blank}")
    @Size(min = 6, max = 64, message = "{validation.system.auth.login.password.size}")
    private String password;

    @Schema(description = "openapi.system.auth.login_command.captcha_code", example = "aB7D")
    private String captchaCode;

    @Schema(description = "openapi.system.auth.login_command.captcha_code_key")
    private String captchaCodeKey;

    public LoginCommand() {
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

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getCaptchaCodeKey() {
        return captchaCodeKey;
    }

    public void setCaptchaCodeKey(String captchaCodeKey) {
        this.captchaCodeKey = captchaCodeKey;
    }
}
