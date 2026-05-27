package com.velox.module.system.auth.session;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.domain.model.UserSession;
import com.velox.module.system.persistence.UserSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DatabaseUserSessionService implements UserSessionService {

    private static final int SESSION_STATUS_ACTIVE = 1;
    private static final int SESSION_STATUS_LOGGED_OUT = 2;
    private static final int NOT_DELETED = 0;

    private final UserSessionMapper userSessionMapper;
    private final SystemAuthProperties.Login.Presence presenceProperties;

    public DatabaseUserSessionService(UserSessionMapper userSessionMapper,
                                      SystemAuthProperties authProperties) {
        this.userSessionMapper = userSessionMapper;
        this.presenceProperties = authProperties.getLogin().getPresence();
    }

    @Override
    public void recordLogin(String userId, String sessionId, String tokenValue) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(sessionId) || !StringUtils.hasText(tokenValue)) {
            return;
        }
        LocalDateTime now = nowUtc();
        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setTokenHash(hashToken(tokenValue));
        session.setStatus(SESSION_STATUS_ACTIVE);
        session.setLoginTime(now);
        session.setLastActiveTime(now);
        session.setLogoutTime(null);
        session.setPresenceExpireTime(resolveLoginPresenceExpiry(now));
        session.setCreateBy(userId);
        session.setUpdateBy(userId);
        session.setDeleted(NOT_DELETED);
        userSessionMapper.insert(session);
    }

    @Override
    public void recordRequestActivity(String userId, String tokenValue) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(tokenValue)) {
            return;
        }
        UserSession session = findByTokenHash(hashToken(tokenValue));
        if (session == null) {
            return;
        }
        LocalDateTime now = nowUtc();
        session.setLastActiveTime(now);
        if (presenceProperties.isRequestHeartbeatEnabled()) {
            session.setPresenceExpireTime(now.plusSeconds(Math.max(1, presenceProperties.getIdleOfflineSeconds())));
        }
        session.setUpdateBy(userId);
        session.setUpdateTime(now);
        userSessionMapper.updateById(session);
    }

    @Override
    public void recordLogout(String userId, String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            return;
        }
        UserSession session = findByTokenHash(hashToken(tokenValue));
        if (session == null) {
            return;
        }
        LocalDateTime now = nowUtc();
        session.setStatus(SESSION_STATUS_LOGGED_OUT);
        session.setLogoutTime(now);
        if (presenceProperties.isLogoutSignalEnabled()) {
            int logoutOfflineSeconds = presenceProperties.getLogoutOfflineSeconds();
            session.setPresenceExpireTime(logoutOfflineSeconds <= 0 ? now : now.plusSeconds(logoutOfflineSeconds));
        }
        if (StringUtils.hasText(userId)) {
            session.setUpdateBy(userId);
        }
        session.setUpdateTime(now);
        userSessionMapper.updateById(session);
    }

    @Override
    public boolean isOnline(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return userSessionMapper.selectCount(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getDeleted, NOT_DELETED)
                .eq(UserSession::getUserId, userId.trim())
                .isNotNull(UserSession::getPresenceExpireTime)
                .gt(UserSession::getPresenceExpireTime, nowUtc())) > 0;
    }

    @Override
    public Map<String, String> resolveStatuses(Collection<String> userIds) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return statuses;
        }

        Set<String> normalizedUserIds = new LinkedHashSet<>();
        for (String userId : userIds) {
            if (StringUtils.hasText(userId)) {
                normalizedUserIds.add(userId.trim());
            }
        }
        if (normalizedUserIds.isEmpty()) {
            return statuses;
        }

        for (String userId : normalizedUserIds) {
            statuses.put(userId, STATUS_OFFLINE);
        }

        List<UserSession> onlineSessions = userSessionMapper.selectList(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getDeleted, NOT_DELETED)
                .in(UserSession::getUserId, normalizedUserIds)
                .isNotNull(UserSession::getPresenceExpireTime)
                .gt(UserSession::getPresenceExpireTime, nowUtc()));
        for (UserSession session : onlineSessions) {
            if (StringUtils.hasText(session.getUserId())) {
                statuses.put(session.getUserId(), STATUS_ONLINE);
            }
        }
        return statuses;
    }

    private UserSession findByTokenHash(String tokenHash) {
        if (!StringUtils.hasText(tokenHash)) {
            return null;
        }
        return userSessionMapper.selectOne(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getDeleted, NOT_DELETED)
                .eq(UserSession::getTokenHash, tokenHash)
                .last("limit 1"));
    }

    private LocalDateTime resolveLoginPresenceExpiry(LocalDateTime now) {
        if (!presenceProperties.isLoginSignalEnabled()) {
            return null;
        }
        return now.plusSeconds(Math.max(1, presenceProperties.getIdleOfflineSeconds()));
    }

    private String hashToken(String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            return null;
        }
        return DigestUtil.sha256Hex(tokenValue.trim());
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
