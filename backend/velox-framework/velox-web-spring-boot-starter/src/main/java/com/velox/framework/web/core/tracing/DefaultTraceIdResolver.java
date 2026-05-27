package com.velox.framework.web.core.tracing;

import com.velox.framework.web.api.tracing.TraceIdAccessor;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import com.velox.framework.web.spi.tracing.TraceIdResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class DefaultTraceIdResolver implements TraceIdResolver {

    private static final String UUID_HYPHEN = "-";
    private static final String EMPTY_TRACE_ID_SEGMENT = "";

    @Override
    public String resolveTraceId(HttpServletRequest request) {
        if (request == null) {
            throw new WebConfigException(WebCommonMessages.HTTP_SERVLET_REQUEST_MUST_NOT_BE_NULL);
        }

        String traceId = request.getHeader(TraceIdAccessor.HEADER_NAME);
        if (StringUtils.hasText(traceId)) {
            return traceId.trim();
        }
        return UUID.randomUUID().toString().replace(UUID_HYPHEN, EMPTY_TRACE_ID_SEGMENT);
    }
}
