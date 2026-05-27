package com.velox.framework.security.support.aspect;

import com.velox.framework.security.api.annotation.RequireAuthenticated;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class SecurityAuthorizationAspect {

    private final SecurityAuthorizationService securityAuthorizationService;

    public SecurityAuthorizationAspect(SecurityAuthorizationService securityAuthorizationService) {
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @Around("@within(requireAuthenticated) || @annotation(requireAuthenticated)")
    public Object aroundAuthenticated(ProceedingJoinPoint joinPoint,
                                      RequireAuthenticated requireAuthenticated) throws Throwable {
        securityAuthorizationService.checkAuthenticated();
        return joinPoint.proceed();
    }

    @Around("@within(requirePermission) || @annotation(requirePermission)")
    public Object aroundPermission(ProceedingJoinPoint joinPoint,
                                   RequirePermission requirePermission) throws Throwable {
        securityAuthorizationService.checkPermission(requirePermission.value());
        return joinPoint.proceed();
    }
}
