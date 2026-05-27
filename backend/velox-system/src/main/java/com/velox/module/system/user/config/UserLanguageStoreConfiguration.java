package com.velox.module.system.user.config;

import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import com.velox.module.system.user.store.InMemoryUserLanguageStore;
import com.velox.module.system.user.store.RedisUserLanguageStore;
import com.velox.module.system.user.store.UserLanguageStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class UserLanguageStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(UserLanguageStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "true", matchIfMissing = true)
    public UserLanguageStore redisUserLanguageStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisUserLanguageStore(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(UserLanguageStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "false")
    public UserLanguageStore inMemoryUserLanguageStore() {
        return new InMemoryUserLanguageStore();
    }
}
