package com.velox.module.system.auth.store;

import com.velox.module.system.auth.properties.SystemAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

public class RedisVerificationCodeStore extends AbstractVerificationCodeStore {

    private static final DefaultRedisScript<Long> CONSUME_AND_COMPARE_SCRIPT = new DefaultRedisScript<>(
            """
                    local stored = redis.call('GET', KEYS[1])
                    if not stored then
                        return 0
                    end
                    redis.call('DEL', KEYS[1])
                    if stored == ARGV[1] then
                        return 2
                    end
                    return 1
                    """,
            Long.class
    );

    private static final DefaultRedisScript<Long> COMPARE_AND_DELETE_IF_MATCH_SCRIPT = new DefaultRedisScript<>(
            """
                    local stored = redis.call('GET', KEYS[1])
                    if not stored then
                        return 0
                    end
                    if stored == ARGV[1] then
                        redis.call('DEL', KEYS[1])
                        return 2
                    end
                    return 1
                    """,
            Long.class
    );

    private static final DefaultRedisScript<Long> SAVE_CODE_IF_ALLOWED_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('EXISTS', KEYS[2]) == 1 then
                        return 0
                    end
                    redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
                    redis.call('SET', KEYS[2], '1', 'EX', ARGV[3])
                    return 1
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    public RedisVerificationCodeStore(StringRedisTemplate stringRedisTemplate, SystemAuthProperties authProperties) {
        super(authProperties);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void saveCaptcha(String key, String code) {
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + key,
                digest(code),
                Duration.ofSeconds(authProperties.getCaptcha().getTtlSeconds())
        );
    }

    @Override
    public VerificationResult consumeCaptcha(String key, String code) {
        return executeConsumeAndCompare(CAPTCHA_PREFIX + key, digest(code));
    }

    @Override
    public boolean captchaExists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(CAPTCHA_PREFIX + key));
    }

    @Override
    public boolean trySaveResetCode(String email, String code) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(RESET_PREFIX + email, RESET_SENT_PREFIX + email),
                digest(code),
                String.valueOf(authProperties.getVerification().getResetCodeTtlSeconds()),
                String.valueOf(authProperties.getVerification().getResetCodeResendIntervalSeconds())
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateResetCode(String email) {
        stringRedisTemplate.delete(List.of(RESET_PREFIX + email, RESET_SENT_PREFIX + email));
    }

    @Override
    public VerificationResult verifyResetCode(String email, String code) {
        return executeCompareAndDeleteIfMatch(RESET_PREFIX + email, digest(code));
    }

    @Override
    public boolean resetCodeExists(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(RESET_PREFIX + email));
    }

    @Override
    public boolean trySaveLoginCode(String target, String code) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(LOGIN_CODE_PREFIX + target, LOGIN_CODE_SENT_PREFIX + target),
                digest(code),
                String.valueOf(authProperties.getVerification().getResetCodeTtlSeconds()),
                String.valueOf(authProperties.getVerification().getResetCodeResendIntervalSeconds())
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateLoginCode(String target) {
        stringRedisTemplate.delete(List.of(LOGIN_CODE_PREFIX + target, LOGIN_CODE_SENT_PREFIX + target));
    }

    @Override
    public VerificationResult verifyLoginCode(String target, String code) {
        return executeCompareAndDeleteIfMatch(LOGIN_CODE_PREFIX + target, digest(code));
    }

    @Override
    public boolean loginCodeExists(String target) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_CODE_PREFIX + target));
    }

    @Override
    public boolean trySaveRebindCode(String scope, String target, String code, int ttlSeconds, int resendIntervalSeconds) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(rebindKey(scope, target), rebindSentKey(scope, target)),
                digest(code),
                String.valueOf(ttlSeconds),
                String.valueOf(resendIntervalSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateRebindCode(String scope, String target) {
        stringRedisTemplate.delete(List.of(rebindKey(scope, target), rebindSentKey(scope, target)));
    }

    @Override
    public VerificationResult verifyRebindCode(String scope, String target, String code) {
        return executeCompareAndDeleteIfMatch(rebindKey(scope, target), digest(code));
    }

    @Override
    public boolean rebindCodeExists(String scope, String target) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(rebindKey(scope, target)));
    }

    @Override
    public boolean trySaveMfaCode(String userId, String code, int ttlSeconds, int resendIntervalSeconds) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(MFA_CODE_PREFIX + userId, MFA_CODE_SENT_PREFIX + userId),
                digest(code),
                String.valueOf(ttlSeconds),
                String.valueOf(resendIntervalSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateMfaCode(String userId) {
        stringRedisTemplate.delete(List.of(MFA_CODE_PREFIX + userId, MFA_CODE_SENT_PREFIX + userId));
    }

    @Override
    public VerificationResult verifyMfaCode(String userId, String code) {
        return executeCompareAndDeleteIfMatch(MFA_CODE_PREFIX + userId, digest(code));
    }

    @Override
    public boolean mfaCodeExists(String userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(MFA_CODE_PREFIX + userId));
    }

    @Override
    public void saveMfaChallenge(String challengeToken, String userId, int ttlSeconds) {
        stringRedisTemplate.opsForValue().set(
                MFA_CHALLENGE_PREFIX + challengeToken,
                userId,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public String consumeMfaChallenge(String challengeToken) {
        String key = MFA_CHALLENGE_PREFIX + challengeToken;
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            return null;
        }
        stringRedisTemplate.delete(key);
        return userId;
    }

    @Override
    public String peekMfaChallenge(String challengeToken) {
        return stringRedisTemplate.opsForValue().get(MFA_CHALLENGE_PREFIX + challengeToken);
    }

    @Override
    public void saveProofTicket(String scene, String proofTicket, String userId, int ttlSeconds) {
        stringRedisTemplate.opsForValue().set(
                proofTicketKey(scene, proofTicket),
                userId,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public String consumeProofTicket(String scene, String proofTicket) {
        String key = proofTicketKey(scene, proofTicket);
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            return null;
        }
        stringRedisTemplate.delete(key);
        return userId;
    }

    @Override
    public String peekProofTicket(String scene, String proofTicket) {
        return stringRedisTemplate.opsForValue().get(proofTicketKey(scene, proofTicket));
    }

    private VerificationResult executeConsumeAndCompare(String redisKey, String digestedCode) {
        Long result = stringRedisTemplate.execute(
                CONSUME_AND_COMPARE_SCRIPT,
                List.of(redisKey),
                digestedCode
        );
        if (Long.valueOf(2L).equals(result)) {
            return VerificationResult.MATCHED;
        }
        if (Long.valueOf(1L).equals(result)) {
            return VerificationResult.INVALID;
        }
        return VerificationResult.EXPIRED;
    }

    private VerificationResult executeCompareAndDeleteIfMatch(String redisKey, String digestedCode) {
        Long result = stringRedisTemplate.execute(
                COMPARE_AND_DELETE_IF_MATCH_SCRIPT,
                List.of(redisKey),
                digestedCode
        );
        if (Long.valueOf(2L).equals(result)) {
            return VerificationResult.MATCHED;
        }
        if (Long.valueOf(1L).equals(result)) {
            return VerificationResult.INVALID;
        }
        return VerificationResult.EXPIRED;
    }
}
