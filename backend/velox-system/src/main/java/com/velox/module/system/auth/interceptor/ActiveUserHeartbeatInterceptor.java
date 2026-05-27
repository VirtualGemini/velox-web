package com.velox.module.system.auth.interceptor;

import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ActiveUserHeartbeatInterceptor implements HandlerInterceptor {

    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;

    public ActiveUserHeartbeatInterceptor(ActiveUserStatusService activeUserStatusService,
                                          SecuritySessionService securitySessionService) {
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String loginId = securitySessionService.currentLoginIdOrNull();
        String tokenValue = securitySessionService.currentTokenOrNull();
        if (loginId != null && !loginId.isBlank()) {
            activeUserStatusService.recordRequestActivity(loginId, tokenValue);
        }
        return true;
    }
}
