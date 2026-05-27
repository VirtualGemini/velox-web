package com.velox.module.system.auth.store;

import com.velox.module.system.auth.properties.SystemAuthProperties;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryVerificationCodeStore extends AbstractVerificationCodeStore {

    private static final int CLEANUP_INTERVAL = 128;

    private final Map<String, ExpiringValue> store = new ConcurrentHashMap<>();
    private final AtomicInteger operationCounter = new AtomicInteger();
    private final Object resetCodeMutex = new Object();
    private final Object loginCodeMutex = new Object();
    private final Object rebindCodeMutex = new Object();
    private final Object mfaCodeMutex = new Object();

    public InMemoryVerificationCodeStore(SystemAuthProperties authProperties) {
        super(authProperties);
    }

    @Override
    public void saveCaptcha(String key, String code) {
        put(CAPTCHA_PREFIX + key, digest(code), Duration.ofSeconds(authProperties.getCaptcha().getTtlSeconds()));
    }

    @Override
    public VerificationResult consumeCaptcha(String key, String code) {
        String storeKey = CAPTCHA_PREFIX + key;
        return consumeAndCompare(storeKey, digest(code));
    }

    @Override
    public boolean captchaExists(String key) {
        return get(CAPTCHA_PREFIX + key) != null;
    }

    @Override
    public boolean trySaveResetCode(String email, String code) {
        synchronized (resetCodeMutex) {
            cleanupExpiredEntriesIfNeeded();
            if (peek(RESET_SENT_PREFIX + email) != null) {
                return false;
            }
            put(
                    RESET_PREFIX + email,
                    digest(code),
                    Duration.ofSeconds(authProperties.getVerification().getResetCodeTtlSeconds())
            );
            put(
                    RESET_SENT_PREFIX + email,
                    "1",
                    Duration.ofSeconds(authProperties.getVerification().getResetCodeResendIntervalSeconds())
            );
            return true;
        }
    }

    @Override
    public void invalidateResetCode(String email) {
        synchronized (resetCodeMutex) {
            store.remove(RESET_PREFIX + email);
            store.remove(RESET_SENT_PREFIX + email);
        }
    }

    @Override
    public VerificationResult verifyResetCode(String email, String code) {
        return compareAndDeleteIfMatch(RESET_PREFIX + email, digest(code));
    }

    @Override
    public boolean resetCodeExists(String email) {
        return get(RESET_PREFIX + email) != null;
    }

    @Override
    public boolean trySaveLoginCode(String target, String code) {
        synchronized (loginCodeMutex) {
            cleanupExpiredEntriesIfNeeded();
            if (peek(LOGIN_CODE_SENT_PREFIX + target) != null) {
                return false;
            }
            put(
                    LOGIN_CODE_PREFIX + target,
                    digest(code),
                    Duration.ofSeconds(authProperties.getVerification().getResetCodeTtlSeconds())
            );
            put(
                    LOGIN_CODE_SENT_PREFIX + target,
                    "1",
                    Duration.ofSeconds(authProperties.getVerification().getResetCodeResendIntervalSeconds())
            );
            return true;
        }
    }

    @Override
    public void invalidateLoginCode(String target) {
        synchronized (loginCodeMutex) {
            store.remove(LOGIN_CODE_PREFIX + target);
            store.remove(LOGIN_CODE_SENT_PREFIX + target);
        }
    }

    @Override
    public VerificationResult verifyLoginCode(String target, String code) {
        return compareAndDeleteIfMatch(LOGIN_CODE_PREFIX + target, digest(code));
    }

    @Override
    public boolean loginCodeExists(String target) {
        return get(LOGIN_CODE_PREFIX + target) != null;
    }

    @Override
    public boolean trySaveRebindCode(String scope, String target, String code, int ttlSeconds, int resendIntervalSeconds) {
        synchronized (rebindCodeMutex) {
            cleanupExpiredEntriesIfNeeded();
            if (peek(rebindSentKey(scope, target)) != null) {
                return false;
            }
            put(rebindKey(scope, target), digest(code), Duration.ofSeconds(ttlSeconds));
            put(rebindSentKey(scope, target), "1", Duration.ofSeconds(resendIntervalSeconds));
            return true;
        }
    }

    @Override
    public void invalidateRebindCode(String scope, String target) {
        synchronized (rebindCodeMutex) {
            store.remove(rebindKey(scope, target));
            store.remove(rebindSentKey(scope, target));
        }
    }

    @Override
    public VerificationResult verifyRebindCode(String scope, String target, String code) {
        return compareAndDeleteIfMatch(rebindKey(scope, target), digest(code));
    }

    @Override
    public boolean rebindCodeExists(String scope, String target) {
        return get(rebindKey(scope, target)) != null;
    }

    @Override
    public boolean trySaveMfaCode(String userId, String code, int ttlSeconds, int resendIntervalSeconds) {
        synchronized (mfaCodeMutex) {
            cleanupExpiredEntriesIfNeeded();
            if (peek(MFA_CODE_SENT_PREFIX + userId) != null) {
                return false;
            }
            put(MFA_CODE_PREFIX + userId, digest(code), Duration.ofSeconds(ttlSeconds));
            put(MFA_CODE_SENT_PREFIX + userId, "1", Duration.ofSeconds(resendIntervalSeconds));
            return true;
        }
    }

    @Override
    public void invalidateMfaCode(String userId) {
        synchronized (mfaCodeMutex) {
            store.remove(MFA_CODE_PREFIX + userId);
            store.remove(MFA_CODE_SENT_PREFIX + userId);
        }
    }

    @Override
    public VerificationResult verifyMfaCode(String userId, String code) {
        return compareAndDeleteIfMatch(MFA_CODE_PREFIX + userId, digest(code));
    }

    @Override
    public boolean mfaCodeExists(String userId) {
        return get(MFA_CODE_PREFIX + userId) != null;
    }

    @Override
    public void saveMfaChallenge(String challengeToken, String userId, int ttlSeconds) {
        put(MFA_CHALLENGE_PREFIX + challengeToken, userId, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public String consumeMfaChallenge(String challengeToken) {
        AtomicReference<String> result = new AtomicReference<>();
        store.compute(MFA_CHALLENGE_PREFIX + challengeToken, (ignored, existing) -> {
            if (existing == null || existing.expiredAtMillis() <= System.currentTimeMillis()) {
                return null;
            }
            result.set(existing.value());
            return null;
        });
        return result.get();
    }

    @Override
    public String peekMfaChallenge(String challengeToken) {
        ExpiringValue value = peek(MFA_CHALLENGE_PREFIX + challengeToken);
        return value == null ? null : value.value();
    }

    @Override
    public void saveProofTicket(String scene, String proofTicket, String userId, int ttlSeconds) {
        put(proofTicketKey(scene, proofTicket), userId, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public String consumeProofTicket(String scene, String proofTicket) {
        AtomicReference<String> result = new AtomicReference<>();
        store.compute(proofTicketKey(scene, proofTicket), (ignored, existing) -> {
            if (existing == null || existing.expiredAtMillis() <= System.currentTimeMillis()) {
                return null;
            }
            result.set(existing.value());
            return null;
        });
        return result.get();
    }

    @Override
    public String peekProofTicket(String scene, String proofTicket) {
        ExpiringValue value = peek(proofTicketKey(scene, proofTicket));
        return value == null ? null : value.value();
    }

    private void put(String key, String value, Duration ttl) {
        cleanupExpiredEntriesIfNeeded();
        store.put(key, new ExpiringValue(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    private ExpiringValue get(String key) {
        cleanupExpiredEntriesIfNeeded();
        ExpiringValue value = store.get(key);
        if (value == null) {
            return null;
        }
        if (value.expiredAtMillis() <= System.currentTimeMillis()) {
            store.remove(key, value);
            return null;
        }
        return value;
    }

    private ExpiringValue peek(String key) {
        ExpiringValue value = store.get(key);
        if (value == null) {
            return null;
        }
        if (value.expiredAtMillis() <= System.currentTimeMillis()) {
            store.remove(key, value);
            return null;
        }
        return value;
    }

    private VerificationResult consumeAndCompare(String key, String expectedDigest) {
        cleanupExpiredEntriesIfNeeded();
        AtomicReference<VerificationResult> result = new AtomicReference<>(VerificationResult.EXPIRED);
        store.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiredAtMillis() <= System.currentTimeMillis()) {
                result.set(VerificationResult.EXPIRED);
                return null;
            }
            if (existing.value().equals(expectedDigest)) {
                result.set(VerificationResult.MATCHED);
            } else {
                result.set(VerificationResult.INVALID);
            }
            return null;
        });
        return result.get();
    }

    private VerificationResult compareAndDeleteIfMatch(String key, String expectedDigest) {
        cleanupExpiredEntriesIfNeeded();
        AtomicReference<VerificationResult> result = new AtomicReference<>(VerificationResult.EXPIRED);
        store.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiredAtMillis() <= System.currentTimeMillis()) {
                result.set(VerificationResult.EXPIRED);
                return null;
            }
            if (existing.value().equals(expectedDigest)) {
                result.set(VerificationResult.MATCHED);
                return null;
            }
            result.set(VerificationResult.INVALID);
            return existing;
        });
        return result.get();
    }

    int storedEntryCount() {
        cleanupExpiredEntries();
        return store.size();
    }

    private void cleanupExpiredEntriesIfNeeded() {
        if (operationCounter.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            cleanupExpiredEntries();
        }
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ExpiringValue> entry : store.entrySet()) {
            ExpiringValue value = entry.getValue();
            if (value.expiredAtMillis() <= now) {
                store.remove(entry.getKey(), value);
            }
        }
    }

    private record ExpiringValue(String value, long expiredAtMillis) {
    }
}
