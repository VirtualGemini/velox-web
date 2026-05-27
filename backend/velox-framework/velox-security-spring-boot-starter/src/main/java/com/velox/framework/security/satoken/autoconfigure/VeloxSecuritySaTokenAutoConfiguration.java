package com.velox.framework.security.satoken.autoconfigure;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.jwt.StpLogicJwtForMixin;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.velox.framework.security.api.authorization.SecurityPermissionProvider;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.api.token.SecurityTokenEngine;
import com.velox.framework.security.api.token.SecurityTokenProvider;
import com.velox.framework.security.api.token.SecurityTokenRuntime;
import com.velox.framework.security.common.constant.SecurityConstants;
import com.velox.framework.security.common.message.SecurityCommonMessages;
import com.velox.framework.security.exception.SecurityConfigException;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.framework.security.support.authorization.SaTokenPermissionProviderAdapter;
import com.velox.framework.security.support.context.SaTokenContextForSpringServlet;
import com.velox.framework.security.support.session.SaTokenSecuritySessionService;
import com.velox.framework.security.support.token.SaTokenJwtTokenProvider;
import com.velox.framework.security.support.token.SaTokenStatefulTokenProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
public class VeloxSecuritySaTokenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = SecurityConstants.SA_TOKEN_STATEFUL_PROVIDER_BEAN_NAME)
    public SecurityTokenProvider veloxSaTokenStatefulTokenProvider(SecurityProperties securityProperties) {
        return new SaTokenStatefulTokenProvider(securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = SecurityConstants.SA_TOKEN_JWT_PROVIDER_BEAN_NAME)
    public SecurityTokenProvider veloxSaTokenJwtTokenProvider(SecurityProperties securityProperties) {
        return new SaTokenJwtTokenProvider(securityProperties);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public SaTokenConfig saTokenConfig(SecurityTokenRuntime runtime) {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName(runtime.getTokenName());
        config.setTimeout(runtime.getTimeout());
        config.setIsConcurrent(runtime.isConcurrent());
        config.setIsShare(runtime.isShare());
        config.setIsLog(runtime.isLogEnabled());
        config.setIsReadHeader(runtime.isReadHeader());
        config.setIsReadCookie(runtime.isReadCookie());
        config.setIsWriteHeader(runtime.isWriteHeader());
        config.setTokenStyle(runtime.getStyle());
        if (runtime.getEngine() == SecurityTokenEngine.JWT_MIXIN
                || runtime.getEngine() == SecurityTokenEngine.JWT_SIMPLE
                || runtime.getEngine() == SecurityTokenEngine.JWT_STATELESS) {
            config.setJwtSecretKey(runtime.getJwtSecret());
        }
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public StpLogic stpLogic(SecurityTokenRuntime runtime) {
        String loginType = runtime.getLoginType() == null || runtime.getLoginType().isBlank()
                ? SecurityConstants.DEFAULT_LOGIN_TYPE
                : runtime.getLoginType();
        return switch (runtime.getEngine()) {
            case STATEFUL -> new StpLogic(loginType);
            case JWT_MIXIN -> new StpLogicJwtForMixin(loginType);
            case JWT_SIMPLE -> new StpLogicJwtForSimple(loginType);
            case JWT_STATELESS -> new StpLogicJwtForStateless(loginType);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public SaTokenContext saTokenContext() {
        return new SaTokenContextForSpringServlet();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecuritySessionService securitySessionService() {
        return new SaTokenSecuritySessionService();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityPermissionProvider.class)
    public SecurityPermissionProvider securityPermissionProvider(ObjectProvider<StpInterface> stpInterfaceProvider) {
        StpInterface stpInterface = stpInterfaceProvider.getIfAvailable();
        if (stpInterface != null) {
            return new SaTokenPermissionProviderAdapter(stpInterface, StpUtil.TYPE);
        }
        throw new SecurityConfigException(SecurityCommonMessages.SECURITY_PERMISSION_PROVIDER_NOT_FOUND);
    }
}
