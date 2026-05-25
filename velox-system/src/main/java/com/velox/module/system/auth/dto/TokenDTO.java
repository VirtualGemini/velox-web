package com.velox.module.system.auth.dto;

public class TokenDTO {
    private String token;
    private Object userInfo;
    /**
     * 第一段登录通过且用户开启了二段验证时返回的临时挑战令牌。
     * 该字段与 token 互斥：mfaChallenge 非空表示需要进入二段验证。
     */
    private String mfaChallenge;
    /**
     * 二段验证目标邮箱（已脱敏），用于前端提示"将向 xxx 发送验证码"。
     * 仅在返回 mfaChallenge 时设置。
     */
    private String mfaEmailMasked;

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

    public String getMfaEmailMasked() {
        return mfaEmailMasked;
    }

    public void setMfaEmailMasked(String mfaEmailMasked) {
        this.mfaEmailMasked = mfaEmailMasked;
    }
}
