package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 解绑 TOTP：必须提交当前认证器生成的口令，证明 owner 在场。
 */
public class MfaTotpDisableCommand {

    @NotBlank
    @Size(min = 6, max = 8)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
