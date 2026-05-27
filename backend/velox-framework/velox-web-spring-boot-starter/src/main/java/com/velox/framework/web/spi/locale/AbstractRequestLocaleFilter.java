package com.velox.framework.web.spi.locale;

import com.velox.framework.web.api.locale.RequestLocaleContext;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

public abstract class AbstractRequestLocaleFilter extends OncePerRequestFilter {

    protected final RequestLocaleResolver requestLocaleResolver;

    protected AbstractRequestLocaleFilter(RequestLocaleResolver requestLocaleResolver) {
        if (requestLocaleResolver == null) {
            throw new WebConfigException(WebCommonMessages.REQUEST_LOCALE_RESOLVER_MUST_NOT_BE_NULL);
        }
        this.requestLocaleResolver = requestLocaleResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Locale locale = resolveLocale(request, response);
            if (locale == null) {
                throw new WebConfigException(WebCommonMessages.RESOLVED_LOCALE_MUST_NOT_BE_NULL);
            }
            RequestLocaleContext.setCurrentLocale(locale);
            LocaleContextHolder.setLocale(locale);
            doFilterWithLocale(request, response, filterChain, locale);
        } finally {
            RequestLocaleContext.clear();
            LocaleContextHolder.resetLocaleContext();
        }
    }

    protected Locale resolveLocale(HttpServletRequest request, HttpServletResponse response) {
        return requestLocaleResolver.resolve(request);
    }

    protected void doFilterWithLocale(HttpServletRequest request,
                                      HttpServletResponse response,
                                      FilterChain filterChain,
                                      Locale locale) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}
