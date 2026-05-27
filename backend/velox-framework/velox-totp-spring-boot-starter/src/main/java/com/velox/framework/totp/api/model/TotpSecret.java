package com.velox.framework.totp.api.model;

import com.velox.framework.totp.support.type.TotpAlgorithm;

/**
 * TOTP 密钥载体，Base32 编码后即为各类 Authenticator 客户端识别的 secret。
 */
public record TotpSecret(
        String base32,
        TotpAlgorithm algorithm,
        int digits,
        int periodSeconds
) {
}
