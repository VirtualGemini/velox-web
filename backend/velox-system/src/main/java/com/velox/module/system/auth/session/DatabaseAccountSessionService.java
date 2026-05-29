package com.velox.module.system.auth.session;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.domain.model.AccountSession;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.persistence.AccountSessionMapper;
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
public class DatabaseAccountSessionService implements AccountSessionService {

    private static final int SESSION_STATUS_ACTIVE = 1;
    private static final int SESSION_STATUS_LOGGED_OUT = 2;
    private static final int NOT_DELETED = 0;

    private final AccountSessionMapper accountSessionMapper;
    private final SystemAuthProperties.Login.Presence presenceProperties;
    private final SystemEntityIdGenerator entityIdGenerator;

    public DatabaseAccountSessionService(AccountSessionMapper accountSessionMapper,
                                         SystemAuthProperties authProperties,
                                         SystemEntityIdGenerator entityIdGenerator) {
        this.accountSessionMapper = accountSessionMapper;
        this.presenceProperties = authProperties.getLogin().getPresence();
        this.entityIdGenerator = entityIdGenerator;
    }

    @Override
    public void recordLogin(String accountId, String sessionId, String tokenValue) {
        if (!StringUtils.hasText(accountId) || !StringUtils.hasText(sessionId) || !StringUtils.hasText(tokenValue)) {
            return;
        }
        LocalDateTime now = nowUtc();
        accountSessionMapper.insert(buildActiveSession(accountId, sessionId, tokenValue, now, resolveLoginPresenceExpiry(now)));
    }

    @Override
    public void recordRequestActivity(String accountId, String tokenValue) {
        if (!StringUtils.hasText(accountId) || !StringUtils.hasText(tokenValue)) {
            return;
        }
        AccountSession session = findByTokenHash(hashToken(tokenValue));
        if (session == null) {
            // 会话记录缺失（例如数据库被重置）但 token 仍有效：以当前请求重建在线会话，
            // 否则在线状态会一直停留在离线，直到用户重新登录。
            restoreSession(accountId, tokenValue);
            return;
        }
        LocalDateTime now = nowUtc();
        session.setLastActiveTime(now);
        if (presenceProperties.isRequestHeartbeatEnabled()) {
            session.setPresenceExpireTime(resolveActivityPresenceExpiry(now));
        }
        session.setUpdateBy(accountId);
        session.setUpdateTime(now);
        accountSessionMapper.updateById(session);
    }

    @Override
    public void recordLogout(String accountId, String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            return;
        }
        AccountSession session = findByTokenHash(hashToken(tokenValue));
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
        if (StringUtils.hasText(accountId)) {
            session.setUpdateBy(accountId);
        }
        session.setUpdateTime(now);
        accountSessionMapper.updateById(session);
    }

    @Override
    public void forceLogoutAll(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return;
        }
        LocalDateTime now = nowUtc();
        List<AccountSession> sessions = accountSessionMapper.selectList(new LambdaQueryWrapper<AccountSession>()
                .eq(AccountSession::getDeleted, NOT_DELETED)
                .eq(AccountSession::getAccountId, accountId.trim())
                .eq(AccountSession::getStatus, SESSION_STATUS_ACTIVE));
        for (AccountSession session : sessions) {
            session.setStatus(SESSION_STATUS_LOGGED_OUT);
            session.setLogoutTime(now);
            session.setPresenceExpireTime(now);
            session.setUpdateBy(accountId.trim());
            session.setUpdateTime(now);
            accountSessionMapper.updateById(session);
        }
    }

    @Override
    public boolean isOnline(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return false;
        }
        return accountSessionMapper.selectCount(new LambdaQueryWrapper<AccountSession>()
                .eq(AccountSession::getDeleted, NOT_DELETED)
                .eq(AccountSession::getAccountId, accountId.trim())
                .isNotNull(AccountSession::getPresenceExpireTime)
                .gt(AccountSession::getPresenceExpireTime, nowUtc())) > 0;
    }

    @Override
    public Map<String, String> resolveStatuses(Collection<String> accountIds) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (accountIds == null || accountIds.isEmpty()) {
            return statuses;
        }

        Set<String> normalizedAccountIds = new LinkedHashSet<>();
        for (String accountId : accountIds) {
            if (StringUtils.hasText(accountId)) {
                normalizedAccountIds.add(accountId.trim());
            }
        }
        if (normalizedAccountIds.isEmpty()) {
            return statuses;
        }

        for (String accountId : normalizedAccountIds) {
            statuses.put(accountId, STATUS_OFFLINE);
        }

        List<AccountSession> onlineSessions = accountSessionMapper.selectList(new LambdaQueryWrapper<AccountSession>()
                .eq(AccountSession::getDeleted, NOT_DELETED)
                .in(AccountSession::getAccountId, normalizedAccountIds)
                .isNotNull(AccountSession::getPresenceExpireTime)
                .gt(AccountSession::getPresenceExpireTime, nowUtc()));
        for (AccountSession session : onlineSessions) {
            if (StringUtils.hasText(session.getAccountId())) {
                statuses.put(session.getAccountId(), STATUS_ONLINE);
            }
        }
        return statuses;
    }

    private AccountSession findByTokenHash(String tokenHash) {
        if (!StringUtils.hasText(tokenHash)) {
            return null;
        }
        return accountSessionMapper.selectOne(new LambdaQueryWrapper<AccountSession>()
                .eq(AccountSession::getDeleted, NOT_DELETED)
                .eq(AccountSession::getTokenHash, tokenHash)
                .last("limit 1"));
    }

    private void restoreSession(String accountId, String tokenValue) {
        LocalDateTime now = nowUtc();
        LocalDateTime presenceExpire = presenceProperties.isRequestHeartbeatEnabled()
                ? resolveActivityPresenceExpiry(now)
                : resolveLoginPresenceExpiry(now);
        String sessionId = entityIdGenerator.nextId(AccountSession.class);
        accountSessionMapper.insert(buildActiveSession(accountId, sessionId, tokenValue, now, presenceExpire));
    }

    private AccountSession buildActiveSession(String accountId, String sessionId, String tokenValue,
                                              LocalDateTime now, LocalDateTime presenceExpire) {
        AccountSession session = new AccountSession();
        session.setId(sessionId);
        session.setAccountId(accountId);
        session.setTokenHash(hashToken(tokenValue));
        session.setStatus(SESSION_STATUS_ACTIVE);
        session.setLoginTime(now);
        session.setLastActiveTime(now);
        session.setLogoutTime(null);
        session.setPresenceExpireTime(presenceExpire);
        session.setCreateBy(accountId);
        session.setUpdateBy(accountId);
        session.setDeleted(NOT_DELETED);
        return session;
    }

    private LocalDateTime resolveActivityPresenceExpiry(LocalDateTime now) {
        return now.plusSeconds(Math.max(1, presenceProperties.getIdleOfflineSeconds()));
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
