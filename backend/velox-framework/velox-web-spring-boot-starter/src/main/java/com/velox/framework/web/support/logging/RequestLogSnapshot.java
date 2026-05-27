package com.velox.framework.web.support.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public record RequestLogSnapshot(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        long durationMillis,
        String traceId,
        Exception exception
) {
}
