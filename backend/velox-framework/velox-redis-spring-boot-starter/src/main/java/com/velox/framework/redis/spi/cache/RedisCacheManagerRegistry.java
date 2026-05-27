package com.velox.framework.redis.spi.cache;

import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.exception.VeloxRedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RedisCacheManagerRegistry {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheManagerRegistry.class);

    private final Map<String, RedisCacheManagerRegistration> registrations;

    public RedisCacheManagerRegistry(Collection<RedisCacheManagerRegistration> registrations) {
        this.registrations = new LinkedHashMap<>();
        for (RedisCacheManagerRegistration registration : registrations) {
            RedisCacheManagerRegistration previous = this.registrations.get(registration.type());
            if (previous == null) {
                this.registrations.put(registration.type(), registration);
                continue;
            }
            if (previous.builtIn() && !registration.builtIn()) {
                this.registrations.put(registration.type(), registration);
                log.info(RedisCommonMessages.REDIS_CACHE_MANAGER_REGISTRATION_OVERRIDDEN,
                        registration.type(),
                        previous.creator().getClass().getName(),
                        registration.creator().getClass().getName());
                continue;
            }
            if (!previous.builtIn() && registration.builtIn()) {
                log.info(RedisCommonMessages.REDIS_CACHE_MANAGER_REGISTRATION_IGNORED,
                        registration.type(),
                        registration.creator().getClass().getName(),
                        previous.creator().getClass().getName());
                continue;
            }
            throw new VeloxRedisException(
                    RedisCommonMessages.REDIS_CACHE_MANAGER_REGISTRATION_CONFLICT.formatted(registration.type()));
        }
    }

    public RedisCacheManagerRegistration require(String type) {
        RedisCacheManagerRegistration registration = registrations.get(type);
        if (registration == null) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_CACHE_MANAGER_TYPE_UNSUPPORTED.formatted(type));
        }
        return registration;
    }
}
