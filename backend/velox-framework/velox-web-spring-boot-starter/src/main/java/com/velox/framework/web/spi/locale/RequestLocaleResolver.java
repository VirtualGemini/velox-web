package com.velox.framework.web.spi.locale;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

public interface RequestLocaleResolver {

    Locale resolve(HttpServletRequest request);
}
