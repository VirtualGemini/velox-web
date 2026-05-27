package com.velox.module.system.auth.config;

import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.framework.web.properties.VeloxProperties;
import com.velox.framework.web.api.path.ApiPathPrefixes;
import com.velox.module.system.auth.interceptor.ActiveUserHeartbeatInterceptor;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityWebMvcConfig implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;
    private final VeloxProperties veloxProperties;
    private final SystemAuthProperties systemAuthProperties;
    private final ActiveUserHeartbeatInterceptor activeUserHeartbeatInterceptor;
    private final SecurityAuthorizationService securityAuthorizationService;

    public SecurityWebMvcConfig(SecurityProperties securityProperties,
                                VeloxProperties veloxProperties,
                                SystemAuthProperties systemAuthProperties,
                                ActiveUserHeartbeatInterceptor activeUserHeartbeatInterceptor,
                                SecurityAuthorizationService securityAuthorizationService) {
        this.securityProperties = securityProperties;
        this.veloxProperties = veloxProperties;
        this.systemAuthProperties = systemAuthProperties;
        this.activeUserHeartbeatInterceptor = activeUserHeartbeatInterceptor;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludes = new ArrayList<>();
        SystemAuthProperties.Interceptor interceptor = systemAuthProperties.getInterceptor();
        interceptor.getAuthExcludeSuffixes().forEach(suffix -> addAuthExclude(excludes, suffix));
        interceptor.getPublicExcludePatterns().forEach(pattern -> addPathExclude(excludes, pattern));

        if (securityProperties.isSwaggerPublicEnabled()) {
            interceptor.getSwaggerPublicExcludePatterns().forEach(excludes::add);
        }

        registry.addInterceptor(new LoginCheckInterceptor(securityAuthorizationService))
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);

        registry.addInterceptor(activeUserHeartbeatInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
    }

    private void addAuthExclude(List<String> excludes, String authSuffix) {
        if (!StringUtils.hasText(authSuffix)) {
            return;
        }
        excludes.add("/auth" + authSuffix);
        String apiPrefix = ApiPathPrefixes.normalize(veloxProperties.getApiPrefix());
        if (!apiPrefix.isEmpty()) {
            excludes.add(apiPrefix + "/auth" + authSuffix);
        }
    }

    private void addPathExclude(List<String> excludes, String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return;
        }
        excludes.add(pattern);
        String apiPrefix = ApiPathPrefixes.normalize(veloxProperties.getApiPrefix());
        if (!apiPrefix.isEmpty()) {
            excludes.add(apiPrefix + pattern);
        }
    }

    private static final class LoginCheckInterceptor implements HandlerInterceptor {

        private final SecurityAuthorizationService securityAuthorizationService;

        private LoginCheckInterceptor(SecurityAuthorizationService securityAuthorizationService) {
            this.securityAuthorizationService = securityAuthorizationService;
        }

        @Override
        public boolean preHandle(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull Object handler) {
            securityAuthorizationService.checkAuthenticated();
            return true;
        }
    }
}
