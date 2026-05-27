package com.velox.module.system.auth.status;

import com.velox.module.system.auth.properties.SystemAuthProperties;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryActiveUserStatusService implements ActiveUserStatusService {

    private static final int CLEANUP_INTERVAL = 128;

    private final SystemAuthProperties.Login.Presence presenceProperties;
    private final Map<String, Long> presenceStore = new ConcurrentHashMap<>();
    private final AtomicInteger operationCounter = new AtomicInteger();

    public InMemoryActiveUserStatusService(SystemAuthProperties authProperties) {
        this.presenceProperties = authProperties.getLogin().getPresence();
    }

    @Override
    public void recordRequestActivity(String userId, String tokenValue) {
        if (!presenceProperties.isRequestHeartbeatEnabled()) {
            return;
        }
        writePresence(userId, Math.max(1, presenceProperties.getIdleOfflineSeconds()));
    }

    @Override
    public void recordLogin(String userId, String sessionId, String tokenValue) {
        if (!presenceProperties.isLoginSignalEnabled()) {
            return;
        }
        writePresence(userId, Math.max(1, presenceProperties.getIdleOfflineSeconds()));
    }

    @Override
    public void recordLogout(String userId, String tokenValue) {
        if (!presenceProperties.isLogoutSignalEnabled() || !StringUtils.hasText(userId)) {
            return;
        }
        int logoutOfflineSeconds = presenceProperties.getLogoutOfflineSeconds();
        if (logoutOfflineSeconds <= 0) {
            presenceStore.remove(userId.trim());
            return;
        }
        writePresence(userId, logoutOfflineSeconds);
    }

    @Override
    public boolean isOnline(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return getExpiry(userId.trim()) != null;
    }

    @Override
    public Map<String, String> resolveStatuses(Collection<String> userIds) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return statuses;
        }
        for (String userId : userIds) {
            if (!StringUtils.hasText(userId)) {
                continue;
            }
            String normalized = userId.trim();
            statuses.put(normalized, getExpiry(normalized) != null ? STATUS_ONLINE : STATUS_OFFLINE);
        }
        return statuses;
    }

    private void writePresence(String userId, int ttlSeconds) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        cleanupExpiredEntriesIfNeeded();
        presenceStore.put(userId.trim(), System.currentTimeMillis() + ttlSeconds * 1000L);
    }

    private Long getExpiry(String userId) {
        cleanupExpiredEntriesIfNeeded();
        Long expiry = presenceStore.get(userId);
        if (expiry == null) {
            return null;
        }
        if (expiry <= System.currentTimeMillis()) {
            presenceStore.remove(userId, expiry);
            return null;
        }
        return expiry;
    }

    int storedPresenceCount() {
        cleanupExpiredEntries();
        return presenceStore.size();
    }

    private void cleanupExpiredEntriesIfNeeded() {
        if (operationCounter.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            cleanupExpiredEntries();
        }
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : presenceStore.entrySet()) {
            Long expiry = entry.getValue();
            if (expiry != null && expiry <= now) {
                presenceStore.remove(entry.getKey(), expiry);
            }
        }
    }
}
