package com.velox.framework.totp.common.message;

public final class TotpCommonMessages {

    public static final String TOTP_CAPABILITY_DISABLED = "Velox totp capability is disabled. Please enable velox.totp.enabled=true.";
    public static final String SECRET_MUST_NOT_BE_NULL = "TOTP secret must not be null";
    public static final String SECRET_MUST_NOT_BE_BLANK = "TOTP secret must not be blank";
    public static final String SECRET_DECODE_FAILED = "TOTP secret is not a valid Base32 string";
    public static final String SECRET_DIGITS_OUT_OF_RANGE = "TOTP secret digits must be between 6 and 10";
    public static final String SECRET_PERIOD_OUT_OF_RANGE = "TOTP secret period-seconds must be >= 1";
    public static final String SECRET_ALGORITHM_MUST_NOT_BE_NULL = "TOTP secret algorithm must not be null";
    public static final String CODE_MUST_NOT_BE_BLANK = "TOTP code must not be blank";
    public static final String CODE_FORMAT_INVALID = "TOTP code must be %d numeric digits";
    public static final String CODE_MISMATCH = "TOTP code mismatch";
    public static final String ISSUER_MUST_NOT_BE_BLANK = "velox.totp.issuer must not be blank";
    public static final String ACCOUNT_MUST_NOT_BE_BLANK = "TOTP account name must not be blank";
    public static final String DIGITS_OUT_OF_RANGE = "velox.totp.digits must be between 6 and 10";
    public static final String PERIOD_OUT_OF_RANGE = "velox.totp.period-seconds must be >= 1";
    public static final String SECRET_SIZE_OUT_OF_RANGE = "velox.totp.secret-size-bytes must be >= 16";
    public static final String VERIFY_WINDOW_OUT_OF_RANGE = "velox.totp.verify-window-steps must be >= 0";
    public static final String ALGORITHM_MUST_NOT_BE_NULL = "velox.totp.algorithm must not be null";
    public static final String HMAC_COMPUTE_FAILED = "Failed to compute HMAC for TOTP";
    public static final String BASE32_INPUT_MUST_NOT_BE_NULL = "Base32 input must not be null";
    public static final String BASE32_ILLEGAL_CHARACTER = "Illegal Base32 character: %s";

    private TotpCommonMessages() {
    }
}
