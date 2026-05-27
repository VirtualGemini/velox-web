package com.velox.framework.web.core.servlet;

import com.velox.framework.web.RequestLocaleFilter;
import com.velox.framework.web.common.path.WebPathConstants;
import com.velox.framework.web.common.servlet.WebFilterNames;
import com.velox.framework.web.spi.servlet.RequestLocaleFilterRegistrationCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class DefaultRequestLocaleFilterRegistrationCustomizer implements RequestLocaleFilterRegistrationCustomizer {

    private static final int REQUEST_LOCALE_FILTER_ORDER = 3;

    @Override
    public FilterRegistrationBean<jakarta.servlet.Filter> register(RequestLocaleFilter filter) {
        FilterRegistrationBean<jakarta.servlet.Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns(WebPathConstants.ALL_SERVLET_URL_PATTERNS);
        registration.setOrder(REQUEST_LOCALE_FILTER_ORDER);
        registration.setName(WebFilterNames.REQUEST_LOCALE_FILTER);
        return registration;
    }
}
