package com.velox.framework.web.support.servlet;

import com.velox.framework.web.spi.timezone.AbstractRequestTimeZoneFilter;
import com.velox.framework.web.spi.timezone.RequestTimeZoneResolver;

public class RequestTimeZoneFilter extends AbstractRequestTimeZoneFilter {

    public RequestTimeZoneFilter(RequestTimeZoneResolver requestTimeZoneResolver) {
        super(requestTimeZoneResolver);
    }
}
