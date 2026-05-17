package com.velox.framework.security.support.token;

import com.velox.framework.security.api.token.SecurityTokenEngine;
import com.velox.framework.security.api.token.SecurityTokenRuntime;
import com.velox.framework.security.common.constant.SecurityConstants;
import com.velox.framework.security.core.token.AbstractSecurityTokenProvider;
import com.velox.framework.security.properties.SecurityProperties;

public class SaTokenStatefulTokenProvider extends AbstractSecurityTokenProvider {

    public SaTokenStatefulTokenProvider(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Override
    public String provider() {
        return SecurityConstants.TOKEN_PROVIDER_SATOKEN;
    }

    @Override
    public String mode() {
        return SecurityConstants.TOKEN_MODE_STATEFUL;
    }

    @Override
    protected void doCustomize(SecurityTokenRuntime runtime) {
        runtime.setEngine(SecurityTokenEngine.STATEFUL);
    }
}
