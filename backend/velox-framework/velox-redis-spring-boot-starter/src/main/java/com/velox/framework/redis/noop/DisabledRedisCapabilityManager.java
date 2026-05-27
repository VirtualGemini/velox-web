package com.velox.framework.redis.noop;

import com.velox.framework.redis.api.manager.VeloxRedisManager;
import com.velox.framework.redis.support.redis.DefaultRedisTemplateCreator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public class DisabledRedisCapabilityManager implements VeloxRedisManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public DisabledRedisCapabilityManager(RedisSerializer<Object> redisValueSerializer) {
        this.redisTemplate = new DefaultRedisTemplateCreator()
                .create(new DisabledRedisConnectionFactory(), redisValueSerializer);
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
