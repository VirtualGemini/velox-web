package com.velox.common.exception;

/**
 * 错误码接口
 * <p>
 * 所有错误码枚举实现此接口，支持 i18n 和结构化错误信息
 */
public interface ErrorCode {

    /**
     * 错误码
     */
    int code();

    /**
     * 默认错误消息
     */
    String message();

    /**
     * i18n 消息 Key
     */
    default String i18nKey() {
        if (this instanceof Enum<?> enumVal) {
            return enumVal.getDeclaringClass().getSimpleName() + "." + enumVal.name();
        }
        return String.valueOf(code());
    }
}
