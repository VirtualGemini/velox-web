package com.velox.framework.redis.core;

import com.velox.framework.redis.api.manager.VeloxRedisCacheManager;
import com.velox.framework.redis.properties.VeloxCacheProperties;
import com.velox.framework.redis.spi.cache.RedisCacheManagerRegistration;
import com.velox.framework.redis.spi.cache.RedisCacheManagerRegistry;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Collection;

public class DefaultRedisCacheCapabilityManager implements VeloxRedisCacheManager {

    private final VeloxRedisCacheManager delegate;

    public DefaultRedisCacheCapabilityManager(
            RedisCacheManagerRegistry registry,
            String type,
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisValueSerializer) {
        RedisCacheManagerRegistration registration = registry.require(type);
        this.delegate = registration.creator().create(
                cacheProperties,
                veloxCacheProperties,
                redisConnectionFactory,
                redisValueSerializer
        );
    }

    @Override
    public Cache getCache(String name) {
        return delegate.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    @Override
    public RedisCacheConfiguration getCacheConfiguration() {
        return delegate.getCacheConfiguration();
    }
}
