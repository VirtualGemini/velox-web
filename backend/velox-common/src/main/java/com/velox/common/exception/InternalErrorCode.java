package com.velox.common.exception;

/**
 * 内部错误码（0-99）
 * <p>
 * 框架/基础设施级别的错误
 */
public enum InternalErrorCode implements ErrorCode {

    INTERNAL_ERROR(1, "系统内部错误"),
    SERVICE_UNAVAILABLE(2, "服务不可用"),
    DATABASE_ERROR(3, "数据库操作异常"),
    CACHE_ERROR(4, "缓存操作异常"),
    NETWORK_ERROR(5, "网络通信异常"),
    CONFIG_ERROR(6, "系统配置异常"),
    SERIALIZATION_ERROR(7, "序列化/反序列化异常"),
    ;

    private final int code;
    private final String message;

    InternalErrorCode(int code, String message) {
        assertRange(code, 0, 99);
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    private static void assertRange(int code, int min, int max) {
        if (code < min || code > max) {
            throw new IllegalArgumentException(
                    "Internal error code must be in range [" + min + ", " + max + "], got: " + code);
        }
    }
}
