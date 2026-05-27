package com.velox.framework.totp.api.model;

import com.velox.framework.totp.common.error.TotpErrorCode;

public record TotpVerifyResult(
        boolean matched,
        int errorCode,
        String message,
        /**
         * 命中的时间步偏移量。0 表示当前窗口，-1 / +1 表示用户使用了相邻窗口。可用于审计或对漂移做统计。
         */
        Integer matchedOffset
) {

    private static final String SUCCESS_MESSAGE = "ok";

    public static TotpVerifyResult success(int matchedOffset) {
        return new TotpVerifyResult(true, TotpErrorCode.OK.code(), SUCCESS_MESSAGE, matchedOffset);
    }

    public static TotpVerifyResult failure(TotpErrorCode code, String message) {
        return new TotpVerifyResult(false, code.code(), message, null);
    }
}

