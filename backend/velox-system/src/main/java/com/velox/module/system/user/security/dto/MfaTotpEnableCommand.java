package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户在绑定向导中提交：原 provision 下发的 secret + 当前认证器显示的口令。
 */
public class MfaTotpEnableCommand {

    @NotBlank
    @Size(max = 128)
    private String secret;

    @NotBlank
    @Size(min = 6, max = 8)
    private String code;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
