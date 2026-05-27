package com.velox.framework.redis.common.message;

public final class RedisCommonMessages {

    public static final String REDIS_CAPABILITY_DISABLED =
            "Velox redis capability is disabled. Please enable velox.redis.enabled=true.";
    public static final String REDIS_CACHE_CAPABILITY_DISABLED =
            "Velox redis cache capability is disabled. Please enable velox.redis.cache.enabled=true.";
    public static final String REDIS_TEMPLATE_TYPE_REQUIRED = "Redis template type must not be blank";
    public static final String REDIS_TEMPLATE_CREATOR_REQUIRED = "Redis template creator must not be null";
    public static final String REDIS_CACHE_MANAGER_TYPE_REQUIRED = "Redis cache manager type must not be blank";
    public static final String REDIS_CACHE_MANAGER_CREATOR_REQUIRED = "Redis cache manager creator must not be null";
    public static final String REDIS_TEMPLATE_TYPE_UNSUPPORTED = "Unsupported redis template type: %s";
    public static final String REDIS_CACHE_MANAGER_TYPE_UNSUPPORTED = "Unsupported redis cache manager type: %s";
    public static final String REDIS_CONNECTION_FACTORY_REQUIRED =
            "RedisConnectionFactory is required for redis cache manager type=%s";
    public static final String REDIS_TEMPLATE_REGISTRATION_CONFLICT =
            "Duplicate redis template registration for type=%s";
    public static final String REDIS_CACHE_MANAGER_REGISTRATION_CONFLICT =
            "Duplicate redis cache manager registration for type=%s";
    public static final String REDIS_TEMPLATE_REGISTRATION_OVERRIDDEN =
            "Overrode built-in redis template registration for type={} from {} to {}";
    public static final String REDIS_TEMPLATE_REGISTRATION_IGNORED =
            "Ignored built-in redis template registration for type={} because custom registration {} is already active";
    public static final String REDIS_CACHE_MANAGER_REGISTRATION_OVERRIDDEN =
            "Overrode built-in redis cache manager registration for type={} from {} to {}";
    public static final String REDIS_CACHE_MANAGER_REGISTRATION_IGNORED =
            "Ignored built-in redis cache manager registration for type={} because custom registration {} is already active";
    public static final String REDIS_DISABLED_OPERATION = "Redis capability disabled for operation={}";
    public static final String REDIS_CACHE_DISABLED_OPERATION = "Redis cache capability disabled for operation={}";

    private RedisCommonMessages() {
    }
}
