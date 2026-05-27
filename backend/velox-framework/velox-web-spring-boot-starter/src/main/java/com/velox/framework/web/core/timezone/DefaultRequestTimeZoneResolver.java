package com.velox.framework.web.core.timezone;

import com.velox.framework.web.api.timezone.RequestTimeZoneContext;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import com.velox.framework.web.spi.timezone.RequestTimeZoneResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.ZoneId;

public class DefaultRequestTimeZoneResolver implements RequestTimeZoneResolver {

    @Override
    public ZoneId resolve(HttpServletRequest request) {
        if (request == null) {
            throw new WebConfigException(WebCommonMessages.HTTP_SERVLET_REQUEST_MUST_NOT_BE_NULL);
        }

        String timeZone = request.getHeader(RequestTimeZoneContext.HEADER_NAME);
        if (!StringUtils.hasText(timeZone)) {
            return RequestTimeZoneContext.getDefaultZoneId();
        }

        try {
            return ZoneId.of(timeZone.trim());
        } catch (DateTimeException ex) {
            return RequestTimeZoneContext.getDefaultZoneId();
        }
    }
}
