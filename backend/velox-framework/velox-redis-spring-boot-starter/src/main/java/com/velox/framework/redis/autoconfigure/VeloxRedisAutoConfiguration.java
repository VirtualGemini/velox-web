package com.velox.framework.redis.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velox.framework.redis.api.manager.VeloxRedisManager;
import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import com.velox.framework.redis.core.DefaultRedisCapabilityManager;
import com.velox.framework.redis.noop.DisabledRedisCapabilityManager;
import com.velox.framework.redis.noop.DisabledRedisConnectionFactory;
import com.velox.framework.redis.properties.VeloxRedisProperties;
import com.velox.framework.redis.spi.redis.RedisTemplateRegistration;
import com.velox.framework.redis.spi.redis.RedisTemplateRegistry;
import com.velox.framework.redis.support.redis.DefaultRedisTemplateCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@AutoConfiguration(before = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(VeloxRedisProperties.class)
public class VeloxRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    @ConditionalOnProperty(
            prefix = RedisPropertyPrefixes.REDIS,
            name = RedisPropertyPrefixes.ENABLED,
            havingValue = RedisPropertyPrefixes.FALSE
    )
    public RedisConnectionFactory disabledRedisConnectionFactory() {
        return new DisabledRedisConnectionFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplateRegistry redisTemplateRegistry(ObjectProvider<RedisTemplateRegistration> registrations) {
        return new RedisTemplateRegistry(registrations.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean(name = RedisAutoConfigurationConstants.DEFAULT_REDIS_TEMPLATE_REGISTRATION_BEAN_NAME)
    public RedisTemplateRegistration defaultRedisTemplateRegistration() {
        return RedisTemplateRegistration.builtIn(
                RedisAutoConfigurationConstants.DEFAULT_REDIS_TEMPLATE_TYPE,
                new DefaultRedisTemplateCreator()
        );
    }

    @Bean(name = RedisAutoConfigurationConstants.REDIS_VALUE_SERIALIZER_BEAN_NAME)
    @ConditionalOnMissingBean(name = RedisAutoConfigurationConstants.REDIS_VALUE_SERIALIZER_BEAN_NAME)
    public RedisSerializer<Object> veloxRedisValueSerializer(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return DefaultRedisTemplateCreator.buildRedisSerializer(objectMapperProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(value = VeloxRedisManager.class, name = "redisTemplate")
    @ConditionalOnProperty(
            prefix = RedisPropertyPrefixes.REDIS,
            name = RedisPropertyPrefixes.ENABLED,
            havingValue = RedisPropertyPrefixes.TRUE,
            matchIfMissing = true
    )
    public VeloxRedisManager veloxRedisManager(
            RedisTemplateRegistry registry,
            VeloxRedisProperties properties,
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> veloxRedisValueSerializer) {
        return new DefaultRedisCapabilityManager(
                registry,
                properties.getTemplateType(),
                redisConnectionFactory,
                veloxRedisValueSerializer
        );
    }

    @Bean
    @ConditionalOnMissingBean(value = VeloxRedisManager.class, name = "redisTemplate")
    @ConditionalOnProperty(
            prefix = RedisPropertyPrefixes.REDIS,
            name = RedisPropertyPrefixes.ENABLED,
            havingValue = RedisPropertyPrefixes.FALSE
    )
    public VeloxRedisManager disabledVeloxRedisManager(
            RedisSerializer<Object> veloxRedisValueSerializer) {
        return new DisabledRedisCapabilityManager(veloxRedisValueSerializer);
    }

    @Bean(name = "redisTemplate")
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(VeloxRedisManager veloxRedisManager) {
        return veloxRedisManager.getRedisTemplate();
    }
}
