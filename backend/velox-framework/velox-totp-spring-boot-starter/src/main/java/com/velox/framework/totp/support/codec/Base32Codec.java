package com.velox.framework.totp.support.codec;

import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.exception.TotpCodecException;

/**
 * RFC 4648 §6 Base32 编解码，使用大写字母表 A-Z 与数字 2-7，无填充时也可解码。
 * 仅服务于 TOTP secret 序列化，故不暴露通用接口。
 */
public final class Base32Codec {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int[] REVERSE = new int[128];
    private static final String EMPTY = "";
    private static final char PADDING_CHAR = '=';

    static {
        java.util.Arrays.fill(REVERSE, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            REVERSE[ALPHABET[i]] = i;
        }
        // 兼容小写字母
        for (int i = 0; i < ALPHABET.length; i++) {
            REVERSE[Character.toLowerCase(ALPHABET[i])] = i;
        }
    }

    private Base32Codec() {
    }

    public static String encode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(ALPHABET[(buffer >> bitsLeft) & 0x1F]);
            }
        }
        if (bitsLeft > 0) {
            sb.append(ALPHABET[(buffer << (5 - bitsLeft)) & 0x1F]);
        }
        return sb.toString();
    }

    public static byte[] decode(String encoded) {
        if (encoded == null) {
            throw new TotpCodecException(TotpCommonMessages.BASE32_INPUT_MUST_NOT_BE_NULL);
        }
        String normalized = stripPadding(encoded);
        if (normalized.isEmpty()) {
            return new byte[0];
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(normalized.length() * 5 / 8);
        int buffer = 0;
        int bitsLeft = 0;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch >= REVERSE.length || REVERSE[ch] < 0) {
                throw new TotpCodecException(TotpCommonMessages.BASE32_ILLEGAL_CHARACTER.formatted(ch));
            }
            buffer = (buffer << 5) | REVERSE[ch];
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                out.write((buffer >> bitsLeft) & 0xFF);
            }
        }
        return out.toByteArray();
    }

    public static boolean isValid(String encoded) {
        try {
            decode(encoded);
            return true;
        } catch (TotpCodecException ignored) {
            return false;
        }
    }

    private static String stripPadding(String encoded) {
        StringBuilder sb = new StringBuilder(encoded.length());
        for (int i = 0; i < encoded.length(); i++) {
            char ch = encoded.charAt(i);
            if (ch == PADDING_CHAR || Character.isWhitespace(ch)) {
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}

