package com.velox.framework.redis.spi.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public interface RedisTemplateCreator {

    RedisTemplate<String, Object> create(
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisValueSerializer);
}
