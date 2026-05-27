package com.velox.framework.web.core.logging;

import com.velox.framework.web.spi.logging.RequestLogHandler;
import com.velox.framework.web.support.logging.RequestLogSnapshot;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequestLogHandler implements RequestLogHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultRequestLogHandler.class);
    private static final String REQUEST_START_LOG_TEMPLATE = ">>> {} {}";
    private static final String REQUEST_ERROR_LOG_TEMPLATE =
            "<<< {} {} | status={} | duration={}ms | traceId={} | error={}";
    private static final String REQUEST_COMPLETE_LOG_TEMPLATE =
            "<<< {} {} | status={} | duration={}ms | traceId={}";

    @Override
    public void logStart(HttpServletRequest request) {
        log.info(REQUEST_START_LOG_TEMPLATE, request.getMethod(), request.getRequestURI());
    }

    @Override
    public void logCompletion(RequestLogSnapshot snapshot) {
        HttpServletRequest request = snapshot.request();
        int status = snapshot.response().getStatus();
        long durationMillis = snapshot.durationMillis();
        String traceId = snapshot.traceId();

        if (snapshot.exception() != null) {
            log.error(REQUEST_ERROR_LOG_TEMPLATE,
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMillis,
                    traceId,
                    snapshot.exception().getMessage());
            return;
        }

        log.info(REQUEST_COMPLETE_LOG_TEMPLATE,
                request.getMethod(),
                request.getRequestURI(),
                status,
                durationMillis,
                traceId);
    }
}
