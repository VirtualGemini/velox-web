package com.velox.framework.web.spi.logging;

import com.velox.framework.web.api.tracing.TraceIdAccessor;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import com.velox.framework.web.support.logging.RequestLogSnapshot;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public abstract class AbstractRequestLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "velox.request.start-time";

    protected final RequestLogHandler requestLogHandler;

    protected AbstractRequestLogInterceptor(RequestLogHandler requestLogHandler) {
        if (requestLogHandler == null) {
            throw new WebConfigException(WebCommonMessages.REQUEST_LOG_HANDLER_MUST_NOT_BE_NULL);
        }
        this.requestLogHandler = requestLogHandler;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        request.setAttribute(START_TIME_ATTR, System.nanoTime());
        requestLogHandler.logStart(request);
        return beforeRequest(request, response, handler);
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        afterRequest(request, response, handler, ex, new RequestLogSnapshot(
                request,
                response,
                handler,
                resolveDurationMillis(request),
                TraceIdAccessor.getCurrentTraceId(),
                ex
        ));
    }

    protected boolean beforeRequest(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler) {
        return true;
    }

    protected void afterRequest(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex,
                                RequestLogSnapshot snapshot) {
        requestLogHandler.logCompletion(snapshot);
    }

    protected long resolveDurationMillis(HttpServletRequest request) {
        Object startTime = request.getAttribute(START_TIME_ATTR);
        if (startTime instanceof Long startedAt) {
            return (System.nanoTime() - startedAt) / 1_000_000;
        }
        return 0L;
    }
}
