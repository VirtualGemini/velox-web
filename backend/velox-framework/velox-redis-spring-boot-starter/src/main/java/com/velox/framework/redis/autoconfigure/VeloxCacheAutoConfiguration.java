package com.velox.framework.redis.autoconfigure;

import com.velox.framework.redis.api.manager.VeloxRedisCacheManager;
import com.velox.framework.redis.core.DefaultRedisCacheCapabilityManager;
import com.velox.framework.redis.noop.DisabledRedisCacheCapabilityManager;
import com.velox.framework.redis.noop.DisabledRedisConnectionFactory;
import com.velox.framework.redis.properties.VeloxCacheProperties;
import com.velox.framework.redis.spi.cache.RedisCacheManagerRegistration;
import com.velox.framework.redis.spi.cache.RedisCacheManagerRegistry;
import com.velox.framework.redis.support.cache.DefaultRedisCacheManagerCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

@AutoConfiguration(after = VeloxRedisAutoConfiguration.class, before = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisCacheManager.class)
@EnableCaching
@EnableConfigurationProperties({CacheProperties.class, VeloxCacheProperties.class})
public class VeloxCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheManagerRegistry redisCacheManagerRegistry(
            ObjectProvider<RedisCacheManagerRegistration> registrations) {
        return new RedisCacheManagerRegistry(registrations.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean(name = RedisAutoConfigurationConstants.DEFAULT_REDIS_CACHE_MANAGER_REGISTRATION_BEAN_NAME)
    public RedisCacheManagerRegistration defaultRedisCacheManagerRegistration() {
        return RedisCacheManagerRegistration.builtIn(
                RedisAutoConfigurationConstants.DEFAULT_REDIS_CACHE_MANAGER_TYPE,
                new DefaultRedisCacheManagerCreator()
        );
    }

    @Bean
    @ConditionalOnMissingBean(value = {VeloxRedisCacheManager.class, CacheManager.class})
    public VeloxRedisCacheManager veloxRedisCacheManager(
            RedisCacheManagerRegistry registry,
            CacheProperties cacheProperties,
            VeloxCacheProperties veloxCacheProperties,
            ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider,
            RedisSerializer<Object> veloxRedisValueSerializer) {
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactoryProvider.getIfAvailable();
        if (!veloxCacheProperties.isEnabled() || isDisabledRedisConnectionFactory(redisConnectionFactory)) {
            return new DisabledRedisCacheCapabilityManager(
                    cacheProperties,
                    veloxCacheProperties,
                    veloxRedisValueSerializer
            );
        }
        return new DefaultRedisCacheCapabilityManager(
                registry,
                veloxCacheProperties.getCacheManagerType(),
                cacheProperties,
                veloxCacheProperties,
                redisConnectionFactory,
                veloxRedisValueSerializer
        );
    }

    @Bean(name = {"redisCacheManager", "cacheManager"})
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public RedisCacheManager redisCacheManager(VeloxRedisCacheManager veloxRedisCacheManager) {
        return (RedisCacheManager) veloxRedisCacheManager;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(value = RedisCacheConfiguration.class, ignored = VeloxRedisCacheManager.class)
    public RedisCacheConfiguration redisCacheConfiguration(VeloxRedisCacheManager veloxRedisCacheManager) {
        return veloxRedisCacheManager.getCacheConfiguration();
    }

    private boolean isDisabledRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        return redisConnectionFactory instanceof DisabledRedisConnectionFactory;
    }
}
