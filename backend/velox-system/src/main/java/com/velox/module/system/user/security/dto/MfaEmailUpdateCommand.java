package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MfaEmailUpdateCommand {

    @NotNull
    private Boolean enabled;

    /** 启用 MFA 时必填；关闭 MFA 时可空 */
    @Size(max = 12)
    private String code;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
