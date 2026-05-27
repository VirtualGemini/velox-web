package com.velox.framework.redis.noop;

import com.velox.framework.redis.api.manager.VeloxRedisCacheManager;
import com.velox.framework.redis.core.TimeoutRedisCacheManager;
import com.velox.framework.redis.properties.VeloxCacheProperties;
import com.velox.framework.redis.support.cache.DefaultRedisCacheManagerCreator;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;

public class DisabledRedisCacheCapabilityManager extends TimeoutRedisCacheManager implements VeloxRedisCacheManager {

    private final RedisCacheConfiguration cacheConfiguration;

    public DisabledRedisCacheCapabilityManager(
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            RedisSerializer<Object> redisValueSerializer) {
        this(new DefaultRedisCacheManagerCreator()
                .createConfiguration(cacheProperties, veloxCacheProperties, redisValueSerializer),
                veloxCacheProperties);
    }

    private DisabledRedisCacheCapabilityManager(
            RedisCacheConfiguration cacheConfiguration,
            VeloxCacheProperties veloxCacheProperties) {
        super(new DisabledRedisCacheWriter(veloxCacheProperties.getDisabledStrategy()), cacheConfiguration);
        this.cacheConfiguration = cacheConfiguration;
    }

    @Override
    public RedisCacheConfiguration getCacheConfiguration() {
        return cacheConfiguration;
    }
}
