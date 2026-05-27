package com.velox.framework.web;

import com.velox.framework.web.core.timezone.DefaultRequestTimeZoneResolver;
import com.velox.framework.web.spi.timezone.AbstractRequestTimeZoneFilter;
import com.velox.framework.web.spi.timezone.RequestTimeZoneResolver;

public class RequestTimeZoneFilter extends AbstractRequestTimeZoneFilter {

    public RequestTimeZoneFilter() {
        this(new DefaultRequestTimeZoneResolver());
    }

    public RequestTimeZoneFilter(RequestTimeZoneResolver requestTimeZoneResolver) {
        super(requestTimeZoneResolver);
    }
}
