package com.velox.framework.web.spi.servlet;

import com.velox.framework.web.TraceIdFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public interface TraceIdFilterRegistrationCustomizer {

    FilterRegistrationBean<Filter> register(TraceIdFilter filter);
}
