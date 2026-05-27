package com.velox.framework.web;

import com.velox.framework.web.core.tracing.DefaultTraceIdResolver;
import com.velox.framework.web.spi.tracing.AbstractTraceIdFilter;
import com.velox.framework.web.spi.tracing.TraceIdResolver;

/**
 * 请求追踪过滤器
 * <p>
 * 为每个请求生成唯一的 traceId，放入 MDC 便于日志追踪
 */
public class TraceIdFilter extends AbstractTraceIdFilter {

    public TraceIdFilter() {
        this(new DefaultTraceIdResolver());
    }

    public TraceIdFilter(TraceIdResolver traceIdResolver) {
        super(traceIdResolver);
    }
}
