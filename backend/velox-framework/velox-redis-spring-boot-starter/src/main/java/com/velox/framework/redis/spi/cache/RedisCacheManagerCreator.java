package com.velox.framework.redis.spi.cache;

import com.velox.framework.redis.api.manager.VeloxRedisCacheManager;
import com.velox.framework.redis.properties.VeloxCacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

public interface RedisCacheManagerCreator {

    VeloxRedisCacheManager create(
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisValueSerializer);

    RedisCacheConfiguration createConfiguration(
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            RedisSerializer<Object> redisValueSerializer);
}
