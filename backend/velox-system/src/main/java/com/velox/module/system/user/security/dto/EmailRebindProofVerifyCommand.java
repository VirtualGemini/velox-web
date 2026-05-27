package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailRebindProofVerifyCommand {

    @NotBlank
    @Size(max = 32)
    private String proofType;

    @Size(max = 12)
    private String currentEmailCode;

    @Size(max = 12)
    private String totpCode;

    @Size(max = 64)
    private String currentPassword;

    public String getProofType() {
        return proofType;
    }

    public void setProofType(String proofType) {
        this.proofType = proofType;
    }

    public String getCurrentEmailCode() {
        return currentEmailCode;
    }

    public void setCurrentEmailCode(String currentEmailCode) {
        this.currentEmailCode = currentEmailCode;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
