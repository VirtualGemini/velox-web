package com.velox.framework.totp.support.type;

/**
 * HMAC 算法。Google Authenticator / Microsoft Authenticator 等多数客户端默认仅支持 SHA1，
 * SHA256 / SHA512 仅在 Authy / Aegis / 1Password / Bitwarden 等较新客户端中可用。
 * <p>除非有明确要求，默认保持 SHA1 以获得最广泛的兼容性。</p>
 */
public enum TotpAlgorithm {

    SHA1("HmacSHA1"),
    SHA256("HmacSHA256"),
    SHA512("HmacSHA512");

    private final String hmacName;

    TotpAlgorithm(String hmacName) {
        this.hmacName = hmacName;
    }

    public String hmacName() {
        return hmacName;
    }

    public String otpAuthName() {
        return name();
    }
}
