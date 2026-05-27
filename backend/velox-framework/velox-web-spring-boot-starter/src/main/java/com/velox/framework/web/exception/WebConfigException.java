package com.velox.framework.web.exception;

public class WebConfigException extends VeloxWebException {

    public WebConfigException(String message) {
        super(message);
    }

    public WebConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
