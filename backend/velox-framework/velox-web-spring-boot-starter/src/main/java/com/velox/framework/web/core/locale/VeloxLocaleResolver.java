package com.velox.framework.web.core.locale;

import com.velox.framework.web.spi.locale.RequestLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class VeloxLocaleResolver implements LocaleResolver {

    private final RequestLocaleResolver requestLocaleResolver;

    public VeloxLocaleResolver(RequestLocaleResolver requestLocaleResolver) {
        this.requestLocaleResolver = requestLocaleResolver;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return requestLocaleResolver.resolve(request);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("Request locale is resolved from request data");
    }
}
