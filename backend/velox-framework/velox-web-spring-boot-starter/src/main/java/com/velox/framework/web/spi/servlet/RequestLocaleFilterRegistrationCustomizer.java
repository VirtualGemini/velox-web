package com.velox.framework.web.spi.servlet;

import com.velox.framework.web.RequestLocaleFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public interface RequestLocaleFilterRegistrationCustomizer {

    FilterRegistrationBean<Filter> register(RequestLocaleFilter filter);
}
