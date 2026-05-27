package com.velox.framework.web.core.locale;

import com.velox.framework.web.api.locale.RequestLocaleContext;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import com.velox.framework.web.spi.locale.RequestLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Locale;

public class DefaultRequestLocaleResolver implements RequestLocaleResolver {

    @Override
    public Locale resolve(HttpServletRequest request) {
        if (request == null) {
            throw new WebConfigException(WebCommonMessages.HTTP_SERVLET_REQUEST_MUST_NOT_BE_NULL);
        }
        String queryLang = request.getParameter("lang");
        if (StringUtils.hasText(queryLang)) {
            return RequestLocaleContext.resolveLocale(queryLang);
        }
        String languageTag = request.getHeader(RequestLocaleContext.HEADER_NAME);
        return RequestLocaleContext.resolveLocale(languageTag);
    }
}
