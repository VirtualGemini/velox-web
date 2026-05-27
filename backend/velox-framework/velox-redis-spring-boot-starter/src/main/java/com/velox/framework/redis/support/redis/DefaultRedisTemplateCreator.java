package com.velox.framework.redis.support.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velox.framework.redis.spi.redis.RedisTemplateCreator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

public class DefaultRedisTemplateCreator implements RedisTemplateCreator {

    @Override
    public RedisTemplate<String, Object> create(
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisValueSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(redisValueSerializer);
        template.setHashValueSerializer(redisValueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    public static RedisSerializer<Object> buildRedisSerializer(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            return new GenericJackson2JsonRedisSerializer()
                    .configure(mapper -> mapper.findAndRegisterModules());
        }
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.findAndRegisterModules();
        return GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(redisObjectMapper)
                .defaultTyping(true)
                .registerNullValueSerializer(true)
                .build();
    }
}
