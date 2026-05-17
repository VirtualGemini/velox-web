package com.velox.framework.security.support.token;

import com.velox.framework.security.api.token.SecurityTokenEngine;
import com.velox.framework.security.api.token.SecurityTokenRuntime;
import com.velox.framework.security.common.constant.SecurityConstants;
import com.velox.framework.security.common.message.SecurityCommonMessages;
import com.velox.framework.security.core.token.AbstractSecurityTokenProvider;
import com.velox.framework.security.exception.SecurityConfigException;
import com.velox.framework.security.properties.SecurityProperties;
import org.springframework.util.StringUtils;

public class SaTokenJwtTokenProvider extends AbstractSecurityTokenProvider {

    public SaTokenJwtTokenProvider(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Override
    public String provider() {
        return SecurityConstants.TOKEN_PROVIDER_SATOKEN_JWT;
    }

    @Override
    public String mode() {
        return SecurityConstants.TOKEN_MODE_JWT;
    }

    @Override
    protected void doCustomize(SecurityTokenRuntime runtime) {
        String strategy = normalize(securityProperties.getToken().getJwt().getStrategy());
        runtime.setJwtSecret(securityProperties.getToken().getJwt().getSecret());
        runtime.setWriteHeader(securityProperties.getToken().getJwt().isWriteHeader());
        runtime.setEngine(switch (strategy) {
            case SecurityConstants.EMPTY, SecurityConstants.JWT_STRATEGY_MIXIN -> SecurityTokenEngine.JWT_MIXIN;
            case SecurityConstants.JWT_STRATEGY_SIMPLE -> SecurityTokenEngine.JWT_SIMPLE;
            case SecurityConstants.JWT_STRATEGY_STATELESS -> SecurityTokenEngine.JWT_STATELESS;
            default -> throw new SecurityConfigException(SecurityCommonMessages.unsupportedJwtStrategy(strategy));
        });
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : SecurityConstants.EMPTY;
    }
}
