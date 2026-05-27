package com.velox.framework.web.spi.timezone;

import jakarta.servlet.http.HttpServletRequest;

import java.time.ZoneId;

public interface RequestTimeZoneResolver {

    ZoneId resolve(HttpServletRequest request);
}
