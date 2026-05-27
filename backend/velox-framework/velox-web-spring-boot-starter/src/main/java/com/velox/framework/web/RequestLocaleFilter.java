package com.velox.framework.web;

import com.velox.framework.web.core.locale.DefaultRequestLocaleResolver;
import com.velox.framework.web.spi.locale.AbstractRequestLocaleFilter;
import com.velox.framework.web.spi.locale.RequestLocaleResolver;

public class RequestLocaleFilter extends AbstractRequestLocaleFilter {

    public RequestLocaleFilter() {
        this(new DefaultRequestLocaleResolver());
    }

    public RequestLocaleFilter(RequestLocaleResolver requestLocaleResolver) {
        super(requestLocaleResolver);
    }
}
