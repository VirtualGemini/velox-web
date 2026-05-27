package com.velox.module.system.user.security.dto;

/**
 * 返回给前端的 TOTP 绑定材料：Base32 密钥 + 可直接生成二维码的 otpauth:// URI。
 * 前端在用户点击"启用 TOTP"时调用 provision 拉取，绑定确认时把 secret 原样回传。
 */
public class MfaTotpProvisionDTO {

    private String secret;
    private String otpAuthUri;
    private String issuer;
    private String accountName;
    private int digits;
    private int periodSeconds;
    private String algorithm;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getOtpAuthUri() {
        return otpAuthUri;
    }

    public void setOtpAuthUri(String otpAuthUri) {
        this.otpAuthUri = otpAuthUri;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
