package com.velox.framework.web.spi.timezone;

import com.velox.framework.web.api.timezone.RequestTimeZoneContext;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;

public abstract class AbstractRequestTimeZoneFilter extends OncePerRequestFilter {

    protected final RequestTimeZoneResolver requestTimeZoneResolver;

    protected AbstractRequestTimeZoneFilter(RequestTimeZoneResolver requestTimeZoneResolver) {
        if (requestTimeZoneResolver == null) {
            throw new WebConfigException(WebCommonMessages.REQUEST_TIME_ZONE_RESOLVER_MUST_NOT_BE_NULL);
        }
        this.requestTimeZoneResolver = requestTimeZoneResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            ZoneId zoneId = resolveZoneId(request, response);
            if (zoneId == null) {
                throw new WebConfigException(WebCommonMessages.RESOLVED_ZONE_ID_MUST_NOT_BE_NULL);
            }
            RequestTimeZoneContext.setCurrentZoneId(zoneId);
            doFilterWithTimeZone(request, response, filterChain, zoneId);
        } finally {
            RequestTimeZoneContext.clear();
        }
    }

    protected ZoneId resolveZoneId(HttpServletRequest request, HttpServletResponse response) {
        return requestTimeZoneResolver.resolve(request);
    }

    protected void doFilterWithTimeZone(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain,
                                        ZoneId zoneId) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}
