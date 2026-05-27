package com.velox.framework.redis.core;

import com.velox.framework.redis.api.manager.VeloxRedisManager;
import com.velox.framework.redis.spi.redis.RedisTemplateRegistration;
import com.velox.framework.redis.spi.redis.RedisTemplateRegistry;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public class DefaultRedisCapabilityManager implements VeloxRedisManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public DefaultRedisCapabilityManager(
            RedisTemplateRegistry registry,
            String type,
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisValueSerializer) {
        RedisTemplateRegistration registration = registry.require(type);
        this.redisTemplate = registration.creator().create(redisConnectionFactory, redisValueSerializer);
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
