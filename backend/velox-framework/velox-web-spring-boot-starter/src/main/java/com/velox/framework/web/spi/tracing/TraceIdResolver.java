package com.velox.framework.web.spi.tracing;

import jakarta.servlet.http.HttpServletRequest;

public interface TraceIdResolver {

    String resolveTraceId(HttpServletRequest request);
}
