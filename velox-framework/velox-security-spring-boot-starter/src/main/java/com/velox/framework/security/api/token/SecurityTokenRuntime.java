package com.velox.framework.security.api.token;

import com.velox.framework.security.common.constant.SecurityConstants;

public class SecurityTokenRuntime {

    private SecurityTokenEngine engine;
    private String tokenName;
    private long timeout;
    private boolean concurrent;
    private boolean share;
    private String style;
    private boolean logEnabled;
    private boolean readHeader;
    private boolean readCookie;
    private boolean writeHeader;
    private String loginType = SecurityConstants.DEFAULT_LOGIN_TYPE;
    private String jwtSecret;

    public SecurityTokenEngine getEngine() {
        return engine;
    }

    public void setEngine(SecurityTokenEngine engine) {
        this.engine = engine;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isShare() {
        return share;
    }

    public void setShare(boolean share) {
        this.share = share;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public boolean isReadHeader() {
        return readHeader;
    }

    public void setReadHeader(boolean readHeader) {
        this.readHeader = readHeader;
    }

    public boolean isReadCookie() {
        return readCookie;
    }

    public void setReadCookie(boolean readCookie) {
        this.readCookie = readCookie;
    }

    public boolean isWriteHeader() {
        return writeHeader;
    }

    public void setWriteHeader(boolean writeHeader) {
        this.writeHeader = writeHeader;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
}
