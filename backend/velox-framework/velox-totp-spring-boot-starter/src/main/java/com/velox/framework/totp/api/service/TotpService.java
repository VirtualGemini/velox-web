package com.velox.framework.totp.api.service;

import com.velox.framework.totp.api.model.TotpProvisioning;
import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;

/**
 * TOTP 能力门面，遵循 RFC 6238。
 * <p>实现需保证生成的 secret / otpauth URI 与下列客户端兼容：
 * Google Authenticator、Microsoft Authenticator、腾讯身份验证器、国产 OTP、
 * Authy、Aegis、1Password、Bitwarden。</p>
 *
 * <p>本接口不感知任何业务实体（用户、安全策略、数据库），只面向标准协议。</p>
 */
public interface TotpService {

    /**
     * 是否处于可用状态。配置 {@code velox.totp.enabled=false} 时返回 false。
     */
    boolean isEnabled();

    /**
     * 生成新的密钥，密钥长度由 {@code velox.totp.secret-size-bytes} 决定。
     */
    TotpSecret generateSecret();

    /**
     * 基于已存在的 Base32 secret 构造 otpauth URI。
     */
    String buildOtpAuthUri(TotpSecret secret, String accountName);

    /**
     * 同时生成 secret 与 otpauth URI，绑定流程的常用入口。
     */
    TotpProvisioning provision(String accountName);

    /**
     * 当前时间窗口对应的口令，主要用于调试与单元测试。
     */
    String currentCode(TotpSecret secret);

    /**
     * 校验用户输入的口令是否落在配置允许的时间窗口内。
     */
    TotpVerifyResult verify(TotpSecret secret, String code);

    /**
     * 使用客户端提供的 base32 secret 进行一次校验。digits / period / algorithm 走全局默认配置。
     */
    TotpVerifyResult verify(String base32Secret, String code);
}
