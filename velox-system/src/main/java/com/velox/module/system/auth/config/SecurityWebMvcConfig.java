package com.velox.module.system.auth.config;

import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.framework.web.properties.VeloxProperties;
import com.velox.framework.web.api.path.ApiPathPrefixes;
import com.velox.module.system.auth.interceptor.ActiveUserHeartbeatInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityWebMvcConfig implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;
    private final VeloxProperties veloxProperties;
    private final ActiveUserHeartbeatInterceptor activeUserHeartbeatInterceptor;
    private final SecurityAuthorizationService securityAuthorizationService;

    public SecurityWebMvcConfig(SecurityProperties securityProperties,
                                VeloxProperties veloxProperties,
                                ActiveUserHeartbeatInterceptor activeUserHeartbeatInterceptor,
                                SecurityAuthorizationService securityAuthorizationService) {
        this.securityProperties = securityProperties;
        this.veloxProperties = veloxProperties;
        this.activeUserHeartbeatInterceptor = activeUserHeartbeatInterceptor;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludes = new ArrayList<>();
        addAuthExclude(excludes, "/login");
        addAuthExclude(excludes, "/login-code/send");
        addAuthExclude(excludes, "/login-code/login");
        addAuthExclude(excludes, "/captcha");
        addAuthExclude(excludes, "/register");
        addAuthExclude(excludes, "/forgot-password/code");
        addAuthExclude(excludes, "/forgot-password/reset");
        addAuthExclude(excludes, "/mfa/challenge/send-code");
        addAuthExclude(excludes, "/mfa/challenge/verify");
        addPublicFileDownloadExclude(excludes);

        if (securityProperties.isSwaggerPublicEnabled()) {
            excludes.add("/swagger-ui/**");
            excludes.add("/swagger-ui.html");
            excludes.add("/v3/api-docs/**");
            excludes.add("/doc.html");
            excludes.add("/webjars/**");
        }

        registry.addInterceptor(new LoginCheckInterceptor(securityAuthorizationService))
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);

        registry.addInterceptor(activeUserHeartbeatInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
    }

    private void addAuthExclude(List<String> excludes, String authSuffix) {
        excludes.add("/auth" + authSuffix);
        String apiPrefix = ApiPathPrefixes.normalize(veloxProperties.getApiPrefix());
        if (!apiPrefix.isEmpty()) {
            excludes.add(apiPrefix + "/auth" + authSuffix);
        }
    }

    private void addPublicFileDownloadExclude(List<String> excludes) {
        excludes.add("/file/*/get/**");
        String apiPrefix = ApiPathPrefixes.normalize(veloxProperties.getApiPrefix());
        if (!apiPrefix.isEmpty()) {
            excludes.add(apiPrefix + "/file/*/get/**");
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
