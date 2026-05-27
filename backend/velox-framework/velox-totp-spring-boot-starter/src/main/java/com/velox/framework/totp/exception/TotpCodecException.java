package com.velox.framework.totp.exception;

public class TotpCodecException extends VeloxTotpException {

    public TotpCodecException(String message) {
        super(message);
    }

    public TotpCodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
