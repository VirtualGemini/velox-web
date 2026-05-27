package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.spi.TotpCodeGenerator;
import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.exception.TotpCodecException;
import com.velox.framework.totp.exception.TotpComputeException;
import com.velox.framework.totp.support.codec.Base32Codec;
import com.velox.framework.totp.support.type.TotpAlgorithm;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.time.Clock;

public class DefaultTotpCodeGenerator implements TotpCodeGenerator {

    private static final int HOTP_COUNTER_BYTES = 8;
    private static final long DECIMAL_BASE = 10L;
    private static final char ZERO_PAD = '0';

    private final Clock clock;

    public DefaultTotpCodeGenerator() {
        this(Clock.systemUTC());
    }

    public DefaultTotpCodeGenerator(Clock clock) {
        this.clock = requireClock(clock);
    }

    @Override
    public String currentCode(TotpSecret secret) {
        return codeAt(secret, clock.instant().getEpochSecond());
    }

    @Override
    public String codeAt(TotpSecret secret, long epochSecond) {
        TotpSecrets.requireValidForGeneration(secret);
        byte[] key = decodeSecret(secret.base32());
        long counter = epochSecond / secret.periodSeconds();
        return formatCode(generateCode(key, counter, secret.algorithm(), secret.digits()), secret.digits());
    }

    long generateCode(byte[] key, long counter, TotpAlgorithm algorithm, int digits) {
        byte[] data = new byte[HOTP_COUNTER_BYTES];
        for (int i = HOTP_COUNTER_BYTES - 1; i >= 0; i--) {
            data[i] = (byte) (counter & 0xFF);
            counter >>= 8;
        }
        byte[] hash = hmac(key, data, algorithm);
        int offset = hash[hash.length - 1] & 0x0F;
        long binary =
                ((long) (hash[offset] & 0x7F) << 24)
                        | ((long) (hash[offset + 1] & 0xFF) << 16)
                        | ((long) (hash[offset + 2] & 0xFF) << 8)
                        | (hash[offset + 3] & 0xFF);
        long modulus = pow10(digits);
        return binary % modulus;
    }

    String formatCode(long code, int digits) {
        StringBuilder sb = new StringBuilder(Long.toString(code));
        while (sb.length() < digits) {
            sb.insert(0, ZERO_PAD);
        }
        return sb.toString();
    }

    private byte[] decodeSecret(String base32Secret) {
        try {
            return Base32Codec.decode(base32Secret);
        } catch (TotpCodecException ex) {
            throw new TotpComputeException(TotpCommonMessages.SECRET_DECODE_FAILED, ex);
        }
    }

    private byte[] hmac(byte[] key, byte[] data, TotpAlgorithm algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm.hmacName());
            mac.init(new SecretKeySpec(key, algorithm.hmacName()));
            return mac.doFinal(data);
        } catch (GeneralSecurityException ex) {
            throw new TotpComputeException(TotpCommonMessages.HMAC_COMPUTE_FAILED, ex);
        }
    }

    private long pow10(int digits) {
        long value = 1L;
        for (int i = 0; i < digits; i++) {
            value *= DECIMAL_BASE;
        }
        return value;
    }

    private Clock requireClock(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        return clock;
    }
}
