package com.velox.framework.redis.core;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

/**
 * 支持 cacheName#ttl 形式自定义过期时间的 RedisCacheManager
 */
public class TimeoutRedisCacheManager extends RedisCacheManager {

    private static final String SPLIT = "#";

    public TimeoutRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        if (StrUtil.isEmpty(name)) {
            return super.createRedisCache(name, cacheConfig);
        }
        String[] names = StrUtil.splitToArray(name, SPLIT);
        if (names.length != 2) {
            return super.createRedisCache(name, cacheConfig);
        }
        if (cacheConfig != null) {
            String ttlStr = StrUtil.subBefore(names[1], StrUtil.COLON, false);
            names[1] = StrUtil.subAfter(names[1], ttlStr, false);
            cacheConfig = cacheConfig.entryTtl(parseDuration(ttlStr));
        }
        return super.createRedisCache(names[0] + names[1], cacheConfig);
    }

    private Duration parseDuration(String ttlStr) {
        String timeUnit = StrUtil.subSuf(ttlStr, -1);
        return switch (timeUnit) {
            case "d" -> Duration.ofDays(removeDurationSuffix(ttlStr));
            case "h" -> Duration.ofHours(removeDurationSuffix(ttlStr));
            case "m" -> Duration.ofMinutes(removeDurationSuffix(ttlStr));
            case "s" -> Duration.ofSeconds(removeDurationSuffix(ttlStr));
            default -> Duration.ofSeconds(Long.parseLong(ttlStr));
        };
    }

    private Long removeDurationSuffix(String ttlStr) {
        return NumberUtil.parseLong(StrUtil.sub(ttlStr, 0, ttlStr.length() - 1));
    }
}
