package com.velox.framework.file.support.diagnostics;

import com.velox.framework.file.api.diagnostics.FileFailureReason;
import com.velox.framework.file.api.diagnostics.FileFailureReasonResolver;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class DefaultFileFailureReasonResolver implements FileFailureReasonResolver {

    private static final FileFailureReason REASON_CAPABILITY_UNAVAILABLE =
            new FileFailureReason("capability_unavailable", "Storage capability is not enabled in the current environment");
    private static final FileFailureReason REASON_CONNECT_FAILED =
            new FileFailureReason("connect_failed", "Unable to connect to target storage service");
    private static final FileFailureReason REASON_CONNECT_TIMEOUT =
            new FileFailureReason("connect_timeout", "Connection to target storage service timed out");
    private static final FileFailureReason REASON_ACCESS_DENIED =
            new FileFailureReason("access_denied", "Access credentials or permissions were rejected");
    private static final FileFailureReason REASON_INIT_FAILED =
            new FileFailureReason("init_failed", "Storage client initialization or connectivity check failed");

    @Override
    public FileFailureReason resolve(Throwable throwable) {
        Throwable root = rootCause(throwable);
        if (root instanceof NoClassDefFoundError) {
            return REASON_CAPABILITY_UNAVAILABLE;
        }
        if (root instanceof ConnectException) {
            return REASON_CONNECT_FAILED;
        }
        if (root instanceof SocketTimeoutException) {
            return REASON_CONNECT_TIMEOUT;
        }
        String message = root.getMessage();
        if (message != null) {
            String normalized = message.toLowerCase();
            if (normalized.contains("connection refused")) {
                return REASON_CONNECT_FAILED;
            }
            if (normalized.contains("timed out") || normalized.contains("timeout")) {
                return REASON_CONNECT_TIMEOUT;
            }
            if (normalized.contains("access denied") || normalized.contains("authentication failed")) {
                return REASON_ACCESS_DENIED;
            }
        }
        return REASON_INIT_FAILED;
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
