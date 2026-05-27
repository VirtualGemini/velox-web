package com.velox.module.system.auth.config;

import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.auth.session.UserSessionService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.store.InMemoryVerificationCodeStore;
import com.velox.module.system.auth.store.RedisVerificationCodeStore;
import com.velox.module.system.auth.store.VerificationCodeStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AuthStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(ActiveUserStatusService.class)
    public ActiveUserStatusService activeUserStatusService(UserSessionService userSessionService) {
        return new ActiveUserStatusService() {
            @Override
            public void recordRequestActivity(String userId, String tokenValue) {
                userSessionService.recordRequestActivity(userId, tokenValue);
            }

            @Override
            public void recordLogin(String userId, String sessionId, String tokenValue) {
                userSessionService.recordLogin(userId, sessionId, tokenValue);
            }

            @Override
            public void recordLogout(String userId, String tokenValue) {
                userSessionService.recordLogout(userId, tokenValue);
            }

            @Override
            public boolean isOnline(String userId) {
                return userSessionService.isOnline(userId);
            }

            @Override
            public java.util.Map<String, String> resolveStatuses(java.util.Collection<String> userIds) {
                return userSessionService.resolveStatuses(userIds);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(VerificationCodeStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "true", matchIfMissing = true)
    public VerificationCodeStore redisVerificationCodeStore(StringRedisTemplate stringRedisTemplate,
                                                            SystemAuthProperties authProperties) {
        return new RedisVerificationCodeStore(stringRedisTemplate, authProperties);
    }

    @Bean
    @ConditionalOnMissingBean(VerificationCodeStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "false")
    public VerificationCodeStore inMemoryVerificationCodeStore(
            SystemAuthProperties authProperties) {
        return new InMemoryVerificationCodeStore(authProperties);
    }
}
