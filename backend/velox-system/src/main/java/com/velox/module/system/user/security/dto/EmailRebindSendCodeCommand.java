package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailRebindSendCodeCommand {

    @NotBlank
    @Email
    @Size(max = 100)
    private String newEmail;

    @NotBlank
    @Size(max = 128)
    private String proofTicket;

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getProofTicket() {
        return proofTicket;
    }

    public void setProofTicket(String proofTicket) {
        this.proofTicket = proofTicket;
    }
}
