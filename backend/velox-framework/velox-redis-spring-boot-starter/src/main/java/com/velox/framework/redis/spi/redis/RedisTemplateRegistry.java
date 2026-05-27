package com.velox.framework.redis.spi.redis;

import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.exception.VeloxRedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RedisTemplateRegistry {

    private static final Logger log = LoggerFactory.getLogger(RedisTemplateRegistry.class);

    private final Map<String, RedisTemplateRegistration> registrations;

    public RedisTemplateRegistry(Collection<RedisTemplateRegistration> registrations) {
        this.registrations = new LinkedHashMap<>();
        for (RedisTemplateRegistration registration : registrations) {
            RedisTemplateRegistration previous = this.registrations.get(registration.type());
            if (previous == null) {
                this.registrations.put(registration.type(), registration);
                continue;
            }
            if (previous.builtIn() && !registration.builtIn()) {
                this.registrations.put(registration.type(), registration);
                log.info(RedisCommonMessages.REDIS_TEMPLATE_REGISTRATION_OVERRIDDEN,
                        registration.type(),
                        previous.creator().getClass().getName(),
                        registration.creator().getClass().getName());
                continue;
            }
            if (!previous.builtIn() && registration.builtIn()) {
                log.info(RedisCommonMessages.REDIS_TEMPLATE_REGISTRATION_IGNORED,
                        registration.type(),
                        registration.creator().getClass().getName(),
                        previous.creator().getClass().getName());
                continue;
            }
            throw new VeloxRedisException(
                    RedisCommonMessages.REDIS_TEMPLATE_REGISTRATION_CONFLICT.formatted(registration.type()));
        }
    }

    public RedisTemplateRegistration require(String type) {
        RedisTemplateRegistration registration = registrations.get(type);
        if (registration == null) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_TEMPLATE_TYPE_UNSUPPORTED.formatted(type));
        }
        return registration;
    }
}
