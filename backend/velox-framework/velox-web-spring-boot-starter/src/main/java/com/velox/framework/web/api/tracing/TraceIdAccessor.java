package com.velox.framework.web.api.tracing;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class TraceIdAccessor {

    public static final String HEADER_NAME = "X-Trace-Id";

    public static final String MDC_KEY = "traceId";

    private TraceIdAccessor() {
    }

    public static String getCurrentTraceId() {
        return MDC.get(MDC_KEY);
    }

    public static void setCurrentTraceId(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            MDC.remove(MDC_KEY);
            return;
        }
        MDC.put(MDC_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
