package com.velox.framework.web.core.servlet;

import com.velox.framework.web.RequestTimeZoneFilter;
import com.velox.framework.web.common.path.WebPathConstants;
import com.velox.framework.web.common.servlet.WebFilterNames;
import com.velox.framework.web.spi.servlet.RequestTimeZoneFilterRegistrationCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class DefaultRequestTimeZoneFilterRegistrationCustomizer implements RequestTimeZoneFilterRegistrationCustomizer {

    private static final int REQUEST_TIME_ZONE_FILTER_ORDER = 2;

    @Override
    public FilterRegistrationBean<jakarta.servlet.Filter> register(RequestTimeZoneFilter filter) {
        FilterRegistrationBean<jakarta.servlet.Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns(WebPathConstants.ALL_SERVLET_URL_PATTERNS);
        registration.setOrder(REQUEST_TIME_ZONE_FILTER_ORDER);
        registration.setName(WebFilterNames.REQUEST_TIME_ZONE_FILTER);
        return registration;
    }
}
