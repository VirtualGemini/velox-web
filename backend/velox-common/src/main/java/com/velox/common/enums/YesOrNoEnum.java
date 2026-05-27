package com.velox.common.enums;

/**
 * 是否枚举
 */
public enum YesOrNoEnum {

    YES(1, "是"),
    NO(0, "否");

    private final int code;
    private final String desc;

    YesOrNoEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
