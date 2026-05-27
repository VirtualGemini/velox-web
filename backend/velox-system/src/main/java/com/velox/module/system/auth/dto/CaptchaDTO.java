package com.velox.module.system.auth.dto;

public class CaptchaDTO {
    private Boolean isCaptchaOn;
    private String captchaCodeKey;
    private String captchaCodeImg;

    public CaptchaDTO() {
    }

    public Boolean getIsCaptchaOn() {
        return isCaptchaOn;
    }

    public void setIsCaptchaOn(Boolean isCaptchaOn) {
        this.isCaptchaOn = isCaptchaOn;
    }

    public String getCaptchaCodeKey() {
        return captchaCodeKey;
    }

    public void setCaptchaCodeKey(String captchaCodeKey) {
        this.captchaCodeKey = captchaCodeKey;
    }

    public String getCaptchaCodeImg() {
        return captchaCodeImg;
    }

    public void setCaptchaCodeImg(String captchaCodeImg) {
        this.captchaCodeImg = captchaCodeImg;
    }
}
