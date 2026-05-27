package com.velox.framework.redis.spi.cache;

import cn.hutool.core.util.StrUtil;
import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.exception.VeloxRedisException;

public record RedisCacheManagerRegistration(
        String type,
        RedisCacheManagerCreator creator,
        boolean builtIn
) {

    public RedisCacheManagerRegistration(String type, RedisCacheManagerCreator creator) {
        this(type, creator, false);
    }

    public RedisCacheManagerRegistration {
        if (StrUtil.isBlank(type)) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_CACHE_MANAGER_TYPE_REQUIRED);
        }
        if (creator == null) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_CACHE_MANAGER_CREATOR_REQUIRED);
        }
    }

    public static RedisCacheManagerRegistration builtIn(String type, RedisCacheManagerCreator creator) {
        return new RedisCacheManagerRegistration(type, creator, true);
    }
}
