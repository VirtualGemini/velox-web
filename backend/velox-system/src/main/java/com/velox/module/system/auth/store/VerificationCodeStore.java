package com.velox.module.system.auth.store;

public interface VerificationCodeStore {

    void saveCaptcha(String key, String code);

    VerificationResult consumeCaptcha(String key, String code);

    boolean captchaExists(String key);

    boolean trySaveResetCode(String email, String code);

    void invalidateResetCode(String email);

    VerificationResult verifyResetCode(String email, String code);

    boolean resetCodeExists(String email);

    /**
     * 保存登录验证码，包含发送频率限制；target 可为邮箱或手机号。
     */
    boolean trySaveLoginCode(String target, String code);

    void invalidateLoginCode(String target);

    VerificationResult verifyLoginCode(String target, String code);

    boolean loginCodeExists(String target);

    /**
     * 账号换绑验证码：scope 用来区分邮箱 / 手机号；target 为新邮箱或新手机号。
     */
    boolean trySaveRebindCode(String scope, String target, String code, int ttlSeconds, int resendIntervalSeconds);

    void invalidateRebindCode(String scope, String target);

    VerificationResult verifyRebindCode(String scope, String target, String code);

    boolean rebindCodeExists(String scope, String target);

    /**
     * 用户启用 MFA 时一次性验证码（绑定到 userId）。
     */
    boolean trySaveMfaCode(String userId, String code, int ttlSeconds, int resendIntervalSeconds);

    void invalidateMfaCode(String userId);

    VerificationResult verifyMfaCode(String userId, String code);

    boolean mfaCodeExists(String userId);

    /**
     * 登录第一段完成后下发的临时挑战令牌 -> userId；虚拟 MFA 设备验证时一次性消费。
     */
    void saveMfaChallenge(String challengeToken, String userId, int ttlSeconds);

    String consumeMfaChallenge(String challengeToken);

    String peekMfaChallenge(String challengeToken);

    void saveProofTicket(String scene, String proofTicket, String userId, int ttlSeconds);

    String consumeProofTicket(String scene, String proofTicket);

    String peekProofTicket(String scene, String proofTicket);

    enum VerificationResult {
        MATCHED,
        INVALID,
        EXPIRED
    }
}
