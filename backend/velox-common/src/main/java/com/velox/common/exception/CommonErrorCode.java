package com.velox.common.exception;

/**
 * 通用错误码
 */
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    ;

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
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
}
