package com.velox.framework.redis.support.cache;

import cn.hutool.core.util.StrUtil;
import com.velox.framework.redis.api.manager.VeloxRedisCacheManager;
import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.exception.VeloxRedisException;
import com.velox.framework.redis.properties.VeloxCacheProperties;
import com.velox.framework.redis.spi.cache.RedisCacheManagerCreator;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

public class DefaultRedisCacheManagerCreator implements RedisCacheManagerCreator {

    @Override
    public VeloxRedisCacheManager create(
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisValueSerializer) {
        if (redisConnectionFactory == null) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_CONNECTION_FACTORY_REQUIRED.formatted(
                    veloxCacheProperties.getCacheManagerType()));
        }
        RedisCacheConfiguration configuration = createConfiguration(
                cacheProperties,
                veloxCacheProperties,
                redisValueSerializer
        );
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(
                redisConnectionFactory,
                BatchStrategies.scan(veloxCacheProperties.getRedisScanBatchSize())
        );
        return new DefaultRedisCacheManager(cacheWriter, configuration);
    }

    @Override
    public RedisCacheConfiguration createConfiguration(
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            RedisSerializer<Object> redisValueSerializer) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration = configuration.computePrefixWith(cacheName -> {
            String keyPrefix = cacheProperties.getRedis().getKeyPrefix();
            if (StringUtils.hasText(keyPrefix)) {
                keyPrefix = keyPrefix.lastIndexOf(StrUtil.COLON) == -1 ? keyPrefix + StrUtil.COLON : keyPrefix;
                return keyPrefix + cacheName + StrUtil.COLON;
            }
            return cacheName + StrUtil.COLON;
        });
        configuration = configuration.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(redisValueSerializer));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null) {
            configuration = configuration.entryTtl(redisProperties.getTimeToLive());
        }
        if (!redisProperties.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }
        return configuration;
    }
}
