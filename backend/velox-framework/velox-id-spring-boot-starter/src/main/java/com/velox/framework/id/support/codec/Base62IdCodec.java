package com.velox.framework.id.support.codec;

import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdCodecNames;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class Base62IdCodec implements IdCodec {

    private static final String CODE_62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final char[] ALPHABET = CODE_62_CHARACTERS.toCharArray();
    private static final int BASE = ALPHABET.length;
    private static final int[] LOOKUP = new int[128];
    private static final String ZORO = "0";

    static {
        Arrays.fill(LOOKUP, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            LOOKUP[ALPHABET[i]] = i;
        }
    }

    @Override
    public String getName() {
        return IdCodecNames.BASE62;
    }

    @Override
    public String encode(String sourceId) {
        long rawId = parseRawId(sourceId);
        if (rawId == 0) {
            return ZORO;
        }
        long current = rawId;
        StringBuilder encoded = new StringBuilder();
        while (current > 0) {
            encoded.append(ALPHABET[(int) (current % BASE)]);
            current /= BASE;
        }
        return encoded.reverse().toString();
    }

    @Override
    public String decode(String encodedId) {
        if (!StringUtils.hasText(encodedId)) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.ENCODED_ID_MUST_NOT_BE_BLANK);
        }

        long decoded = 0L;
        String normalized = encodedId.trim();
        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);
            int digit = current < LOOKUP.length ? LOOKUP[current] : -1;
            if (digit < 0) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.invalidBase62Character(current));
            }
            if (decoded > (Long.MAX_VALUE - digit) / BASE) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.ENCODED_ID_EXCEEDS_LONG_RANGE);
            }
            decoded = decoded * BASE + digit;
        }
        return String.valueOf(decoded);
    }

    private long parseRawId(String sourceId) {
        if (!StringUtils.hasText(sourceId)) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.SOURCE_ID_MUST_NOT_BE_BLANK);
        }
        String normalized = sourceId.trim();
        try {
            long rawId = Long.parseLong(normalized);
            if (rawId < 0) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.RAW_ID_MUST_NOT_BE_NEGATIVE);
            }
            return rawId;
        } catch (NumberFormatException exception) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.SOURCE_ID_MUST_BE_NUMERIC, exception);
        }
    }
}
