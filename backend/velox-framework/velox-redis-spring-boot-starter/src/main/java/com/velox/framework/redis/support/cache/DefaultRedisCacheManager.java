package com.velox.framework.redis.support.cache;

import com.velox.framework.redis.api.manager.VeloxRedisCacheManager;
import com.velox.framework.redis.core.TimeoutRedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class DefaultRedisCacheManager extends TimeoutRedisCacheManager implements VeloxRedisCacheManager {

    private final RedisCacheConfiguration cacheConfiguration;

    public DefaultRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfiguration) {
        super(cacheWriter, cacheConfiguration);
        this.cacheConfiguration = cacheConfiguration;
    }

    @Override
    public RedisCacheConfiguration getCacheConfiguration() {
        return cacheConfiguration;
    }
}
