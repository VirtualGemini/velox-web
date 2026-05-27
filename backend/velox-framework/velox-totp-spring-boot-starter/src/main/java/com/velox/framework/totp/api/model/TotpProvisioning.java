package com.velox.framework.totp.api.model;

/**
 * 绑定阶段返回给前端的最小开屏信息：Base32 secret 与 otpauth:// URI。
 * 客户端可直接渲染二维码或让用户手动输入 base32。
 */
public record TotpProvisioning(
        TotpSecret secret,
        String otpAuthUri,
        String issuer,
        String accountName
) {
}
