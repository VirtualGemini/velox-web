package com.velox.framework.web.core.servlet;

import com.velox.framework.web.TraceIdFilter;
import com.velox.framework.web.common.path.WebPathConstants;
import com.velox.framework.web.common.servlet.WebFilterNames;
import com.velox.framework.web.spi.servlet.TraceIdFilterRegistrationCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class DefaultTraceIdFilterRegistrationCustomizer implements TraceIdFilterRegistrationCustomizer {

    private static final int TRACE_ID_FILTER_ORDER = 1;

    @Override
    public FilterRegistrationBean<jakarta.servlet.Filter> register(TraceIdFilter filter) {
        FilterRegistrationBean<jakarta.servlet.Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns(WebPathConstants.ALL_SERVLET_URL_PATTERNS);
        registration.setOrder(TRACE_ID_FILTER_ORDER);
        registration.setName(WebFilterNames.TRACE_ID_FILTER);
        return registration;
    }
}
