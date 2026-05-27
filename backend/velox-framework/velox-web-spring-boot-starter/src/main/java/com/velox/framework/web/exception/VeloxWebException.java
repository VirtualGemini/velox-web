package com.velox.framework.web.exception;

public class VeloxWebException extends RuntimeException {

    public VeloxWebException(String message) {
        super(message);
    }

    public VeloxWebException(String message, Throwable cause) {
        super(message, cause);
    }
}
