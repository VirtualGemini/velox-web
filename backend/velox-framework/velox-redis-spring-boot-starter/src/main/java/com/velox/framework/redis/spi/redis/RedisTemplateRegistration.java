package com.velox.framework.redis.spi.redis;

import cn.hutool.core.util.StrUtil;
import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.exception.VeloxRedisException;

public record RedisTemplateRegistration(
        String type,
        RedisTemplateCreator creator,
        boolean builtIn
) {

    public RedisTemplateRegistration(String type, RedisTemplateCreator creator) {
        this(type, creator, false);
    }

    public RedisTemplateRegistration {
        if (StrUtil.isBlank(type)) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_TEMPLATE_TYPE_REQUIRED);
        }
        if (creator == null) {
            throw new VeloxRedisException(RedisCommonMessages.REDIS_TEMPLATE_CREATOR_REQUIRED);
        }
    }

    public static RedisTemplateRegistration builtIn(String type, RedisTemplateCreator creator) {
        return new RedisTemplateRegistration(type, creator, true);
    }
}
