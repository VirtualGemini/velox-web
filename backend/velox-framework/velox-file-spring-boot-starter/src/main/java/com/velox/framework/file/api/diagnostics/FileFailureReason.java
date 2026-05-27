package com.velox.framework.file.api.diagnostics;

public record FileFailureReason(
        String code,
        String message
) {
}
