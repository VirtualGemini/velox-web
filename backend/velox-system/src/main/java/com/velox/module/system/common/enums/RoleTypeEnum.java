package com.velox.module.system.common.enums;

public enum RoleTypeEnum {

    SYSTEM(0, "内置"),
    CUSTOM(1, "自定义");

    private final int code;
    private final String desc;

    RoleTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Integer code) {
        if (code == null) {
            return "";
        }
        for (RoleTypeEnum item : values()) {
            if (item.code == code) {
                return item.desc;
            }
        }
        return "";
    }
}
