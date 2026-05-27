package com.velox.framework.persistence.exception;

public class VeloxPersistenceException extends RuntimeException {

    public VeloxPersistenceException(String message) {
        super(message);
    }

    public VeloxPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
