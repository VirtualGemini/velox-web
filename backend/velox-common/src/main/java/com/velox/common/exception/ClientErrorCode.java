package com.velox.common.exception;

/**
 * 客户端错误码（100-999）
 * <p>
 * 客户端请求相关的错误
 */
public enum ClientErrorCode implements ErrorCode {

    BAD_REQUEST(100, "请求参数错误"),
    UNAUTHORIZED(101, "未认证或认证已过期"),
    FORBIDDEN(103, "没有访问权限"),
    NOT_FOUND(104, "请求资源不存在"),
    METHOD_NOT_ALLOWED(105, "请求方法不允许"),
    CONFLICT(106, "资源冲突"),
    TOO_MANY_REQUESTS(107, "请求过于频繁"),
    VALIDATION_ERROR(108, "参数校验失败"),
    PAYLOAD_TOO_LARGE(109, "请求数据过大"),
    ;

    private final int code;
    private final String message;

    ClientErrorCode(int code, String message) {
        assertRange(code, 100, 999);
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
                    "Client error code must be in range [" + min + ", " + max + "], got: " + code);
        }
    }
}
