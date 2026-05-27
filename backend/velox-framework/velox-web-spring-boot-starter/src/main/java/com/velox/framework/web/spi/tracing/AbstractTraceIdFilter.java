package com.velox.framework.web.spi.tracing;

import com.velox.framework.web.api.tracing.TraceIdAccessor;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public abstract class AbstractTraceIdFilter extends OncePerRequestFilter {

    protected final TraceIdResolver traceIdResolver;

    protected AbstractTraceIdFilter(TraceIdResolver traceIdResolver) {
        if (traceIdResolver == null) {
            throw new WebConfigException(WebCommonMessages.TRACE_ID_RESOLVER_MUST_NOT_BE_NULL);
        }
        this.traceIdResolver = traceIdResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = resolveTraceId(request, response);
            if (!StringUtils.hasText(traceId)) {
                throw new WebConfigException(WebCommonMessages.RESOLVED_TRACE_ID_MUST_NOT_BE_BLANK);
            }
            TraceIdAccessor.setCurrentTraceId(traceId);
            writeTraceId(response, traceId);
            doFilterWithTraceId(request, response, filterChain, traceId);
        } finally {
            TraceIdAccessor.clear();
        }
    }

    protected String resolveTraceId(HttpServletRequest request, HttpServletResponse response) {
        return traceIdResolver.resolveTraceId(request);
    }

    protected void writeTraceId(HttpServletResponse response, String traceId) {
        response.setHeader(TraceIdAccessor.HEADER_NAME, traceId);
    }

    protected void doFilterWithTraceId(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain,
                                       String traceId) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}
