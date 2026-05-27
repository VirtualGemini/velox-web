package com.velox.framework.redis.api.manager;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

public interface VeloxRedisCacheManager extends CacheManager {

    RedisCacheConfiguration getCacheConfiguration();
}
