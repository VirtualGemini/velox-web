package com.velox.module.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.domain.model.BaseEntity;

import java.time.LocalDateTime;

@TableName("sys_user_security")
public class UserSecurity extends BaseEntity {

    private String userId;

    /** 逗号分隔: password,email_code */
    private String loginMethods;

    private Integer mfaEmailEnabled;

    private Integer mfaTotpEnabled;

    private String mfaTotpSecret;

    private LocalDateTime emailVerifiedAt;

    private LocalDateTime lastPasswordChangeAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = normalizeIdentifier(userId);
    }

    public String getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(String loginMethods) {
        this.loginMethods = loginMethods;
    }

    public Integer getMfaEmailEnabled() {
        return mfaEmailEnabled;
    }

    public void setMfaEmailEnabled(Integer mfaEmailEnabled) {
        this.mfaEmailEnabled = mfaEmailEnabled;
    }

    public Integer getMfaTotpEnabled() {
        return mfaTotpEnabled;
    }

    public void setMfaTotpEnabled(Integer mfaTotpEnabled) {
        this.mfaTotpEnabled = mfaTotpEnabled;
    }

    public String getMfaTotpSecret() {
        return mfaTotpSecret;
    }

    public void setMfaTotpSecret(String mfaTotpSecret) {
        this.mfaTotpSecret = mfaTotpSecret;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getLastPasswordChangeAt() {
        return lastPasswordChangeAt;
    }

    public void setLastPasswordChangeAt(LocalDateTime lastPasswordChangeAt) {
        this.lastPasswordChangeAt = lastPasswordChangeAt;
    }
}
