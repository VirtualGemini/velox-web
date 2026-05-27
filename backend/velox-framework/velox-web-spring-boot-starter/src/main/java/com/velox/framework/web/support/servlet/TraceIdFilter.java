package com.velox.framework.web.support.servlet;

import com.velox.framework.web.spi.tracing.AbstractTraceIdFilter;
import com.velox.framework.web.spi.tracing.TraceIdResolver;

public class TraceIdFilter extends AbstractTraceIdFilter {

    public TraceIdFilter(TraceIdResolver traceIdResolver) {
        super(traceIdResolver);
    }
}
