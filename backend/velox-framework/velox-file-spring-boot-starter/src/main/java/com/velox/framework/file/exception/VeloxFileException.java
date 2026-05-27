package com.velox.framework.file.exception;

public class VeloxFileException extends RuntimeException {

    public VeloxFileException(String message) {
        super(message);
    }

    public VeloxFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
