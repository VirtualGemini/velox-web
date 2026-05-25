package com.velox.module.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MfaChallengeVerifyCommand {

    @NotBlank
    @Size(max = 64)
    private String challenge;

    @NotBlank
    @Size(max = 12)
    private String code;

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
