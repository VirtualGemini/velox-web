package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.spi.TotpCodeGenerator;
import com.velox.framework.totp.api.spi.TotpVerifier;
import com.velox.framework.totp.common.error.TotpErrorCode;
import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.properties.VeloxTotpProperties;

import java.time.Clock;
import java.util.regex.Pattern;

public class DefaultTotpVerifier implements TotpVerifier {

    private static final Pattern NUMERIC = Pattern.compile("^\\d+$");

    private final VeloxTotpProperties properties;
    private final TotpCodeGenerator codeGenerator;
    private final Clock clock;

    public DefaultTotpVerifier(VeloxTotpProperties properties, TotpCodeGenerator codeGenerator) {
        this(properties, codeGenerator, Clock.systemUTC());
    }

    public DefaultTotpVerifier(VeloxTotpProperties properties, TotpCodeGenerator codeGenerator, Clock clock) {
        this.properties = requireProperties(properties);
        this.codeGenerator = requireCodeGenerator(codeGenerator);
        this.clock = requireClock(clock);
    }

    @Override
    public TotpVerifyResult verify(TotpSecret secret, String code) {
        TotpVerifyResult secretCheck = TotpSecrets.validateForVerification(secret);
        if (secretCheck != null) {
            return secretCheck;
        }
        TotpVerifyResult formatCheck = validateCodeFormat(code, secret.digits());
        if (formatCheck != null) {
            return formatCheck;
        }
        int window = properties.getVerifyWindowSteps();
        String actual = code.trim();
        long now = clock.instant().getEpochSecond();
        for (int offset = -window; offset <= window; offset++) {
            long candidateEpochSecond = now + ((long) offset * secret.periodSeconds());
            if (constantTimeEquals(codeGenerator.codeAt(secret, candidateEpochSecond), actual)) {
                return TotpVerifyResult.success(offset);
            }
        }
        return TotpVerifyResult.failure(TotpErrorCode.CODE_MISMATCH, TotpCommonMessages.CODE_MISMATCH);
    }

    private TotpVerifyResult validateCodeFormat(String code, int digits) {
        if (code == null || code.isBlank()) {
            return TotpVerifyResult.failure(TotpErrorCode.CODE_BLANK, TotpCommonMessages.CODE_MUST_NOT_BE_BLANK);
        }
        String trimmed = code.trim();
        if (trimmed.length() != digits || !NUMERIC.matcher(trimmed).matches()) {
            return TotpVerifyResult.failure(TotpErrorCode.CODE_FORMAT_INVALID,
                    TotpCommonMessages.CODE_FORMAT_INVALID.formatted(digits));
        }
        return null;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ actual.charAt(i);
        }
        return diff == 0;
    }

    private VeloxTotpProperties requireProperties(VeloxTotpProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        properties.validate();
        return properties;
    }

    private TotpCodeGenerator requireCodeGenerator(TotpCodeGenerator codeGenerator) {
        if (codeGenerator == null) {
            throw new IllegalArgumentException("codeGenerator must not be null");
        }
        return codeGenerator;
    }

    private Clock requireClock(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        return clock;
    }
}
