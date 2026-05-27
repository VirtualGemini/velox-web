package com.velox.module.system.auth.dto;

public class TokenDTO {
    private String token;
    private Object userInfo;
    /**
     * 第一段登录通过且用户开启了虚拟 MFA 设备验证时返回的临时挑战令牌。
     * 该字段与 token 互斥：mfaChallenge 非空表示需要进入虚拟 MFA 设备验证。
     */
    private String mfaChallenge;
    /**
     * 当前挑战需要走的验证方式："email" 或 "totp"。仅在返回 mfaChallenge 时设置。
     */
    private String mfaType;
    /**
     * 虚拟 MFA 设备验证目标邮箱（已脱敏），用于前端提示"将向 xxx 发送验证码"。
     * 仅在 mfaType=email 时设置。
     */
    private String mfaEmailMasked;
    /**
     * TOTP 期望的口令长度（位数），用于前端输入框约束。仅在 mfaType=totp 时设置。
     */
    private Integer mfaTotpDigits;

    public TokenDTO() {
    }

    public TokenDTO(String token, Object userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    public TokenDTO(String token, Object userInfo, String mfaChallenge) {
        this.token = token;
        this.userInfo = userInfo;
        this.mfaChallenge = mfaChallenge;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.userInfo = userInfo;
    }

    public String getMfaChallenge() {
        return mfaChallenge;
    }

    public void setMfaChallenge(String mfaChallenge) {
        this.mfaChallenge = mfaChallenge;
    }

    public String getMfaType() {
        return mfaType;
    }

    public void setMfaType(String mfaType) {
        this.mfaType = mfaType;
    }

    public String getMfaEmailMasked() {
        return mfaEmailMasked;
    }

    public void setMfaEmailMasked(String mfaEmailMasked) {
        this.mfaEmailMasked = mfaEmailMasked;
    }

    public Integer getMfaTotpDigits() {
        return mfaTotpDigits;
    }

    public void setMfaTotpDigits(Integer mfaTotpDigits) {
        this.mfaTotpDigits = mfaTotpDigits;
    }
}
