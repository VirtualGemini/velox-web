package com.velox.framework.security.exception;

public class SecurityAuthorizationException extends RuntimeException {

    public SecurityAuthorizationException(String message) {
        super(message);
    }

    public SecurityAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
