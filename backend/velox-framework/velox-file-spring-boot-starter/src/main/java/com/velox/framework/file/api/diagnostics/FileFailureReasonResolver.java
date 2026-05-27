package com.velox.framework.file.api.diagnostics;

public interface FileFailureReasonResolver {

    FileFailureReason resolve(Throwable throwable);
}
