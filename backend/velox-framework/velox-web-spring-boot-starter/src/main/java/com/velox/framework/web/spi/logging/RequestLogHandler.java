package com.velox.framework.web.spi.logging;

import com.velox.framework.web.support.logging.RequestLogSnapshot;
import jakarta.servlet.http.HttpServletRequest;

public interface RequestLogHandler {

    void logStart(HttpServletRequest request);

    void logCompletion(RequestLogSnapshot snapshot);
}
