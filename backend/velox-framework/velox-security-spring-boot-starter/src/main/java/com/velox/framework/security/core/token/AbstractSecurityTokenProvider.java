package com.velox.framework.security.core.token;

import com.velox.framework.security.api.token.SecurityTokenRuntime;
import com.velox.framework.security.api.token.SecurityTokenProvider;
import com.velox.framework.security.properties.SecurityProperties;

public abstract class AbstractSecurityTokenProvider implements SecurityTokenProvider {

    protected final SecurityProperties securityProperties;

    protected AbstractSecurityTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public void customize(SecurityTokenRuntime runtime) {
        SecurityTokenRuntime baseRuntime = createBaseRuntime();
        runtime.setTokenName(baseRuntime.getTokenName());
        runtime.setTimeout(baseRuntime.getTimeout());
        runtime.setConcurrent(baseRuntime.isConcurrent());
        runtime.setShare(baseRuntime.isShare());
        runtime.setStyle(baseRuntime.getStyle());
        runtime.setLogEnabled(baseRuntime.isLogEnabled());
        runtime.setReadHeader(baseRuntime.isReadHeader());
        runtime.setReadCookie(baseRuntime.isReadCookie());
        runtime.setWriteHeader(baseRuntime.isWriteHeader());
        runtime.setLoginType(baseRuntime.getLoginType());
        runtime.setJwtSecret(baseRuntime.getJwtSecret());
        doCustomize(runtime);
    }

    protected SecurityTokenRuntime createBaseRuntime() {
        SecurityProperties.Token token = securityProperties.getToken();
        SecurityTokenRuntime runtime = new SecurityTokenRuntime();
        runtime.setTokenName(token.getTokenName());
        runtime.setTimeout(token.getTimeout());
        runtime.setConcurrent(token.isConcurrent());
        runtime.setShare(token.isShare());
        runtime.setStyle(token.getStyle());
        runtime.setLogEnabled(token.isLogEnabled());
        runtime.setReadHeader(token.isReadHeader());
        runtime.setReadCookie(token.isReadCookie());
        runtime.setWriteHeader(resolveWriteHeader());
        return runtime;
    }

    protected boolean resolveWriteHeader() {
        return securityProperties.getToken().isWriteHeader();
    }

    protected abstract void doCustomize(SecurityTokenRuntime runtime);
}
