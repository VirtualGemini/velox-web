package com.velox.framework.web.spi.servlet;

import com.velox.framework.web.RequestTimeZoneFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public interface RequestTimeZoneFilterRegistrationCustomizer {

    FilterRegistrationBean<Filter> register(RequestTimeZoneFilter filter);
}
