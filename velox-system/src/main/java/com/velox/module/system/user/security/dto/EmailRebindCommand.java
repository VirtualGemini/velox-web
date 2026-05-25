package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailRebindCommand {

    @NotBlank
    @Email
    @Size(max = 100)
    private String newEmail;

    @NotBlank
    @Size(max = 12)
    private String code;

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
