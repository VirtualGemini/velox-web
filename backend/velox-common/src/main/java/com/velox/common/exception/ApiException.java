package com.velox.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 统一业务异常
 * <p>
 * 所有业务异常均通过此异常抛出，携带 ErrorCode 实现类型安全的错误处理。
 * 支持 i18n 消息和附加数据（payload）。
 */
public class ApiException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(ApiException.class);

    private final ErrorCode errorCode;
    private final String message;
    private final Object[] args;
    private HashMap<String, Object> payload;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.message = errorCode.message();
        this.args = null;
    }

    public ApiException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.message(), args));
        this.errorCode = errorCode;
        this.message = formatMessage(errorCode.message(), args);
        this.args = args;
    }

    public ApiException(Throwable cause, ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.message(), args), cause);
        this.errorCode = errorCode;
        this.message = formatMessage(errorCode.message(), args);
        this.args = args;
    }

    /**
     * 添加附加数据
     */
    public ApiException with(String key, Object value) {
        if (this.payload == null) {
            this.payload = new HashMap<>();
        }
        this.payload.put(key, value);
        return this;
    }

    @Override
    public String getMessage() {
        // 懒解析：每次按当前 LocaleContextHolder 解析，确保使用请求线程的 Locale
        String i18n = resolveI18n(errorCode, args);
        return i18n != null ? i18n : message;
    }

    /**
     * 获取原始消息（非 i18n）
     */
    public String getRawMessage() {
        return message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HashMap<String, Object> getPayload() {
        return payload;
    }

    private static String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        try {
            return String.format(template, args);
        } catch (Exception e) {
            log.warn("Failed to format error message: {}", template, e);
            return template;
        }
    }

    private static String resolveI18n(ErrorCode errorCode, Object... args) {
        try {
            return MessageUtils.message(errorCode.i18nKey(), args);
        } catch (Exception e) {
            // i18n 不可用时静默降级
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiException that = (ApiException) o;
        return errorCode == that.errorCode &&
               java.util.Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(errorCode, message);
    }
}
