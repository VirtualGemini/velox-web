package com.velox.module.system.user.security.dto;

public class EmailRebindProofDTO {

    private String proofTicket;

    private Integer expiresInSeconds;

    public String getProofTicket() {
        return proofTicket;
    }

    public void setProofTicket(String proofTicket) {
        this.proofTicket = proofTicket;
    }

    public Integer getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Integer expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
