package com.velox.framework.web;

import java.time.ZoneId;

public final class RequestTimeZoneContext {

    public static final String HEADER_NAME = com.velox.framework.web.api.timezone.RequestTimeZoneContext.HEADER_NAME;

    private RequestTimeZoneContext() {
    }

    public static ZoneId getCurrentZoneId() {
        return com.velox.framework.web.api.timezone.RequestTimeZoneContext.getCurrentZoneId();
    }

    public static void setCurrentZoneId(String timeZone) {
        com.velox.framework.web.api.timezone.RequestTimeZoneContext.setCurrentZoneId(timeZone);
    }

    public static void setCurrentZoneId(ZoneId zoneId) {
        com.velox.framework.web.api.timezone.RequestTimeZoneContext.setCurrentZoneId(zoneId);
    }

    public static void clear() {
        com.velox.framework.web.api.timezone.RequestTimeZoneContext.clear();
    }
}
