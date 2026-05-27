package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.common.error.TotpErrorCode;
import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.exception.TotpConfigException;
import com.velox.framework.totp.support.codec.Base32Codec;

final class TotpSecrets {

    private static final int MIN_DIGITS = 6;
    private static final int MAX_DIGITS = 10;
    private static final int MIN_PERIOD_SECONDS = 1;

    private TotpSecrets() {
    }

    static void requireValidForGeneration(TotpSecret secret) {
        if (secret == null) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_MUST_NOT_BE_NULL);
        }
        if (secret.base32() == null || secret.base32().isBlank()) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_MUST_NOT_BE_BLANK);
        }
        if (secret.algorithm() == null) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_ALGORITHM_MUST_NOT_BE_NULL);
        }
        if (secret.digits() < MIN_DIGITS || secret.digits() > MAX_DIGITS) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_DIGITS_OUT_OF_RANGE);
        }
        if (secret.periodSeconds() < MIN_PERIOD_SECONDS) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_PERIOD_OUT_OF_RANGE);
        }
        if (!Base32Codec.isValid(secret.base32())) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_DECODE_FAILED);
        }
    }

    static TotpVerifyResult validateForVerification(TotpSecret secret) {
        if (secret == null) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_INVALID, TotpCommonMessages.SECRET_MUST_NOT_BE_NULL);
        }
        if (secret.base32() == null || secret.base32().isBlank()) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_BLANK, TotpCommonMessages.SECRET_MUST_NOT_BE_BLANK);
        }
        if (secret.algorithm() == null) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_ALGORITHM_INVALID,
                    TotpCommonMessages.SECRET_ALGORITHM_MUST_NOT_BE_NULL);
        }
        if (secret.digits() < MIN_DIGITS || secret.digits() > MAX_DIGITS) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_DIGITS_INVALID,
                    TotpCommonMessages.SECRET_DIGITS_OUT_OF_RANGE);
        }
        if (secret.periodSeconds() < MIN_PERIOD_SECONDS) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_PERIOD_INVALID,
                    TotpCommonMessages.SECRET_PERIOD_OUT_OF_RANGE);
        }
        if (!Base32Codec.isValid(secret.base32())) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_INVALID, TotpCommonMessages.SECRET_DECODE_FAILED);
        }
        return null;
    }
}
