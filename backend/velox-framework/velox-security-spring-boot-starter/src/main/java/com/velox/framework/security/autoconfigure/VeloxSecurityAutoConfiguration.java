package com.velox.framework.security.autoconfigure;

import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import com.velox.framework.security.api.authorization.SecurityPermissionProvider;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.api.token.SecurityTokenRuntime;
import com.velox.framework.security.api.token.SecurityTokenProvider;
import com.velox.framework.security.common.message.SecurityCommonMessages;
import com.velox.framework.security.exception.SecurityConfigException;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.framework.security.support.authorization.DefaultSecurityAuthorizationService;
import com.velox.framework.security.support.aspect.SecurityAuthorizationAspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class VeloxSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityTokenRuntime securityTokenRuntime(SecurityProperties securityProperties,
                                                     ObjectProvider<SecurityTokenProvider> tokenProvidersProvider) {
        return buildSecurityRuntime(selectSecurityTokenProvider(securityProperties, tokenProvidersProvider));
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityAuthorizationService securityAuthorizationService(SecuritySessionService securitySessionService,
                                                                     SecurityPermissionProvider securityPermissionProvider) {
        return new DefaultSecurityAuthorizationService(securitySessionService, securityPermissionProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityAuthorizationAspect securityAuthorizationAspect(SecurityAuthorizationService securityAuthorizationService) {
        return new SecurityAuthorizationAspect(securityAuthorizationService);
    }

    private SecurityTokenProvider selectSecurityTokenProvider(SecurityProperties securityProperties,
                                                             ObjectProvider<SecurityTokenProvider> tokenProvidersProvider) {
        SecurityProperties.Token tokenProperties = securityProperties.getToken();
        List<SecurityTokenProvider> candidates = tokenProvidersProvider.orderedStream()
                .filter(SecurityTokenProvider::isAvailable)
                .toList();

        List<SecurityTokenProvider> matches = candidates.stream()
                .filter(provider -> provider.supports(tokenProperties))
                .toList();

        if (matches.isEmpty()) {
            throw new SecurityConfigException(
                    SecurityCommonMessages.tokenProviderNotFound(tokenProperties.getMode(), tokenProperties.getProvider())
            );
        }

        List<SecurityTokenProvider> customMatches = matches.stream()
                .filter(provider -> !provider.isBuiltIn())
                .toList();
        if (customMatches.size() == 1) {
            return customMatches.get(0);
        }
        if (customMatches.size() > 1) {
            throw new SecurityConfigException(
                    SecurityCommonMessages.tokenProviderDuplicated(tokenProperties.getMode(), tokenProperties.getProvider())
            );
        }

        if (matches.size() > 1) {
            throw new SecurityConfigException(
                    SecurityCommonMessages.tokenProviderDuplicated(tokenProperties.getMode(), tokenProperties.getProvider())
            );
        }
        return matches.get(0);
    }

    private SecurityTokenRuntime buildSecurityRuntime(SecurityTokenProvider securityTokenProvider) {
        SecurityTokenRuntime runtime = new SecurityTokenRuntime();
        securityTokenProvider.customize(runtime);
        if (runtime.getEngine() == null) {
            throw new SecurityConfigException(SecurityCommonMessages.SECURITY_TOKEN_PROVIDER_ENGINE_REQUIRED);
        }
        return runtime;
    }
}
