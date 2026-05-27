package com.velox.framework.web.api.timezone;

import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class RequestTimeZoneContext {

    public static final String HEADER_NAME = "X-Time-Zone";

    private static final ZoneId DEFAULT_ZONE_ID = ZoneOffset.UTC;

    private static final ThreadLocal<ZoneId> CONTEXT = new ThreadLocal<>();

    private RequestTimeZoneContext() {
    }

    public static ZoneId getCurrentZoneId() {
        ZoneId zoneId = CONTEXT.get();
        return zoneId == null ? DEFAULT_ZONE_ID : zoneId;
    }

    public static ZoneId getDefaultZoneId() {
        return DEFAULT_ZONE_ID;
    }

    public static void setCurrentZoneId(String timeZone) {
        CONTEXT.set(resolveZoneId(timeZone));
    }

    public static void setCurrentZoneId(ZoneId zoneId) {
        CONTEXT.set(zoneId == null ? DEFAULT_ZONE_ID : zoneId);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    private static ZoneId resolveZoneId(String timeZone) {
        if (!StringUtils.hasText(timeZone)) {
            return DEFAULT_ZONE_ID;
        }
        try {
            return ZoneId.of(timeZone.trim());
        } catch (DateTimeException ex) {
            return DEFAULT_ZONE_ID;
        }
    }
}
