package com.velox.framework.redis.properties;

import com.velox.framework.redis.autoconfigure.RedisAutoConfigurationConstants;
import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import com.velox.framework.redis.common.type.RedisDisabledStrategyType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(RedisPropertyPrefixes.REDIS_CACHE)
public class VeloxCacheProperties {

    private boolean enabled = true;
    private Integer redisScanBatchSize = 30;
    private String cacheManagerType = RedisAutoConfigurationConstants.DEFAULT_REDIS_CACHE_MANAGER_TYPE;
    private RedisDisabledStrategyType disabledStrategy = RedisDisabledStrategyType.FAIL_FAST;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getRedisScanBatchSize() {
        return redisScanBatchSize;
    }

    public void setRedisScanBatchSize(Integer redisScanBatchSize) {
        this.redisScanBatchSize = redisScanBatchSize;
    }

    public String getCacheManagerType() {
        return cacheManagerType;
    }

    public void setCacheManagerType(String cacheManagerType) {
        this.cacheManagerType = cacheManagerType;
    }

    public RedisDisabledStrategyType getDisabledStrategy() {
        return disabledStrategy;
    }

    public void setDisabledStrategy(RedisDisabledStrategyType disabledStrategy) {
        this.disabledStrategy = disabledStrategy;
    }
}
