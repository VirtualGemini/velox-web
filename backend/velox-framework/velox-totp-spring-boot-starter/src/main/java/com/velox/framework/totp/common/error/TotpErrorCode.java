package com.velox.framework.totp.common.error;

import java.util.Arrays;

public enum TotpErrorCode {

    OK(0),
    DISABLED(-2),
    UNKNOWN(-1),
    SECRET_BLANK(1001),
    SECRET_INVALID(1002),
    CODE_BLANK(1003),
    CODE_FORMAT_INVALID(1004),
    CODE_MISMATCH(1005),
    ACCOUNT_BLANK(1006),
    ISSUER_BLANK(1007),
    SECRET_ALGORITHM_INVALID(1008),
    SECRET_DIGITS_INVALID(1009),
    SECRET_PERIOD_INVALID(1010);

    private final int code;

    TotpErrorCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static TotpErrorCode fromCode(int code) {
        return Arrays.stream(values())
                .filter(value -> value.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
