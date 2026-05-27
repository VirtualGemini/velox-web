package com.velox.module.system.auth.status;

import com.velox.module.system.auth.properties.SystemAuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RedisActiveUserStatusService implements ActiveUserStatusService {

    private static final String PRESENCE_KEY_PREFIX = "auth:presence:user:";
    private static final String PRESENCE_ONLINE = "online";

    private final StringRedisTemplate stringRedisTemplate;
    private final SystemAuthProperties.Login.Presence presenceProperties;

    public RedisActiveUserStatusService(StringRedisTemplate stringRedisTemplate, SystemAuthProperties authProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.presenceProperties = authProperties.getLogin().getPresence();
    }

    @Override
    public void recordRequestActivity(String userId, String tokenValue) {
        if (!presenceProperties.isRequestHeartbeatEnabled()) {
            return;
        }
        writePresence(userId, PRESENCE_ONLINE, Math.max(1, presenceProperties.getIdleOfflineSeconds()));
    }

    @Override
    public void recordLogin(String userId, String sessionId, String tokenValue) {
        if (!presenceProperties.isLoginSignalEnabled()) {
            return;
        }
        writePresence(userId, PRESENCE_ONLINE, Math.max(1, presenceProperties.getIdleOfflineSeconds()));
    }

    @Override
    public void recordLogout(String userId, String tokenValue) {
        if (!presenceProperties.isLogoutSignalEnabled() || !StringUtils.hasText(userId)) {
            return;
        }
        int logoutOfflineSeconds = presenceProperties.getLogoutOfflineSeconds();
        if (logoutOfflineSeconds <= 0) {
            stringRedisTemplate.delete(buildKey(userId));
            return;
        }
        // 显式退出后保留在线状态一段时间，TTL 到期后自然转为离线。
        writePresence(userId, PRESENCE_ONLINE, logoutOfflineSeconds);
    }

    @Override
    public boolean isOnline(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return PRESENCE_ONLINE.equals(stringRedisTemplate.opsForValue().get(buildKey(userId)));
    }

    @Override
    public Map<String, String> resolveStatuses(Collection<String> userIds) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return statuses;
        }

        List<String> normalizedUserIds = new ArrayList<>();
        List<String> redisKeys = new ArrayList<>();
        for (String userId : userIds) {
            if (StringUtils.hasText(userId)) {
                normalizedUserIds.add(userId.trim());
                redisKeys.add(buildKey(userId));
            }
        }

        List<String> presenceValues = redisKeys.isEmpty() ? List.of() : stringRedisTemplate.opsForValue().multiGet(redisKeys);
        for (int i = 0; i < normalizedUserIds.size(); i++) {
            String userId = normalizedUserIds.get(i);
            String presence = presenceValues != null && i < presenceValues.size() ? presenceValues.get(i) : null;
            statuses.put(userId, PRESENCE_ONLINE.equals(presence) ? STATUS_ONLINE : STATUS_OFFLINE);
        }
        return statuses;
    }

    private void writePresence(String userId, String presence, int ttlSeconds) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
                buildKey(userId),
                presence,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    private String buildKey(String userId) {
        return PRESENCE_KEY_PREFIX + userId.trim();
    }
}
