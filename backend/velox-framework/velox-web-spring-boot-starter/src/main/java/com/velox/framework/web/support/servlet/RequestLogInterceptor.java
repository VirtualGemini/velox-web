package com.velox.framework.web.support.servlet;

import com.velox.framework.web.core.logging.DefaultRequestLogHandler;
import com.velox.framework.web.spi.logging.RequestLogHandler;
import com.velox.framework.web.spi.logging.AbstractRequestLogInterceptor;

public class RequestLogInterceptor extends AbstractRequestLogInterceptor {

    public RequestLogInterceptor() {
        this(new DefaultRequestLogHandler());
    }

    public RequestLogInterceptor(RequestLogHandler requestLogHandler) {
        super(requestLogHandler);
    }
}
