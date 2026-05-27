package com.velox.module.system.web;

import com.velox.common.exception.ClientErrorCode;
import com.velox.common.result.Result;
import com.velox.framework.security.exception.SecurityAuthenticationException;
import com.velox.framework.security.exception.SecurityAuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SecurityExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionAdvice.class);

    @ExceptionHandler(SecurityAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleSecurityAuthenticationException(SecurityAuthenticationException exception, HttpServletRequest request) {
        log.warn("Unauthorized [{}] {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return Result.fail(ClientErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityAuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleSecurityAuthorizationException(SecurityAuthorizationException exception, HttpServletRequest request) {
        log.warn("Forbidden [{}] {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return Result.fail(ClientErrorCode.FORBIDDEN);
    }
}
