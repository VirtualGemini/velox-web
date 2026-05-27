package com.velox.framework.redis.api.manager;

import org.springframework.data.redis.core.RedisTemplate;

public interface VeloxRedisManager {

    RedisTemplate<String, Object> getRedisTemplate();
}
