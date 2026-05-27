<h2 align="center" id="top">Velox Redis Spring Boot Starter</h2>
<p align="center">一个独立、可热插拔的 Redis 能力 starter，聚焦可替换的 RedisTemplate 装配、可插拔缓存管理策略、显式 disabled 行为，以及 Spring Boot 自动装配。</p>
<div align="center"><a href="./README.md">English</a> | 简体中文</div>

<br />

## 概览

`velox-redis-spring-boot-starter` 是一个面向 Spring Boot 的独立 Redis 能力模块。
它的目标是在保持标准 Spring 注入路径稳定的前提下，允许开发者替换模板创建逻辑、缓存管理器创建逻辑、序列化器，以及 disabled 模式行为。

该 starter 重点解决五件事：

- 为业务模块提供稳定的 `RedisTemplate` 和 `RedisCacheManager` 注入路径
- 通过 SPI 提供 Redis template 与 cache manager 的热插拔扩展点
- 在 disabled 模式下保持 bean 可注入，而不是让注入链路塌陷
- 提供 starter 自有的默认 serializer Bean，并允许完全覆盖
- 通过 Spring Boot 自动装配独立启用，不依赖 `system` 或 `infra`

## 架构分层

该模块延续 `email` 和 `file` starter 的分层风格：

- `api`：公开给使用方的管理器契约，如 `VeloxRedisManager`、`VeloxRedisCacheManager`
- `spi`：官方支持的扩展缝，如 `RedisTemplateCreator`、`RedisCacheManagerCreator` 及其 registration
- `core`：默认能力管理器与支持 TTL 语义的缓存管理器基类
- `support`：内建 template creator 与 cache manager creator
- `noop`：disabled 模式下的 connection factory、template manager、cache manager、cache writer
- `exception` 与 `common`：模块内统一异常、消息、常量与 disabled 策略枚举
- `autoconfigure`：Spring Boot 自动装配入口与配置开关

## 内建能力

starter 默认内建一套 Redis template 策略和一套 cache manager 策略：

- template type：`default`
- cache manager type：`default`

它们都以 built-in registration 的形式注册。
如果业务方提供相同 type 名称的自定义 registration，会覆盖内建默认实现。

## 标准 Bean 暴露

starter 会继续暴露标准 Spring bean：

- `redisTemplate`
- `redisCacheManager`
- `cacheManager`
- `RedisCacheConfiguration`

这意味着业务模块仍然可以直接注入标准 Spring Redis 类型，而平台侧可以通过 SPI 替换底层实现。

## 配置说明

基础配置示例：

```yaml
velox:
  redis:
    enabled: true
    template-type: default
    cache:
      enabled: true
      cache-manager-type: default
      redis-scan-batch-size: 30
      disabled-strategy: FAIL_FAST
spring:
  cache:
    redis:
      time-to-live: 30m
      key-prefix: velox:
```

### Redis 能力开关

```yaml
velox:
  redis:
    enabled: false
```

关闭后：

- 当上下文里没有其他 `RedisConnectionFactory` 时，starter 会提供 disabled 版工厂，保持注入路径存在
- `redisTemplate` 仍然会注册，注入链路不塌陷
- 运行期 Redis IO 会显式失败，而不是静默消失
- registration 元数据仍然可用于能力治理与探测

### Cache 能力开关

```yaml
velox:
  redis:
    cache:
      enabled: false
```

关闭后：

- `redisCacheManager` 和 `cacheManager` 仍然会注册并保持可注入
- 缓存元数据与 `RedisCacheConfiguration` 仍然可用
- 运行期缓存行为由 `velox.redis.cache.disabled-strategy` 决定

支持的 disabled 策略：

- `FAIL_FAST`：缓存操作抛出 `VeloxRedisException`
- `NOOP`：缓存操作只记录 warning，并表现为 no-op

## Serializer 策略

starter 会暴露一个默认 Bean：`veloxRedisValueSerializer`。

行为如下：

- 如果应用里已经有 `ObjectMapper`，starter 会先 `copy()` 一份，再基于副本构建 Redis 专用 serializer
- 如果应用里没有 `ObjectMapper`，starter 会创建一个独立的 `GenericJackson2JsonRedisSerializer`
- 如果应用要完全接管 serializer，只需覆盖 `veloxRedisValueSerializer` Bean

这样既能复用应用侧的大部分 Jackson 配置，又不会直接污染主 `ObjectMapper`。

## 扩展示例

注册一个自定义 Redis template 策略：

```java
@Bean
RedisTemplateRegistration customRedisTemplateRegistration() {
    return new RedisTemplateRegistration(
            "custom",
            (connectionFactory, redisValueSerializer) -> {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);
                template.setKeySerializer(RedisSerializer.string());
                template.setValueSerializer(redisValueSerializer);
                template.afterPropertiesSet();
                return template;
            }
    );
}
```

注册一个自定义 cache manager 策略：

```java
@Bean
RedisCacheManagerRegistration customRedisCacheManagerRegistration() {
    return new RedisCacheManagerRegistration(
            "custom",
            new RedisCacheManagerCreator() {
                @Override
                public VeloxRedisCacheManager create(
                        CacheProperties cacheProperties,
                        VeloxCacheProperties veloxCacheProperties,
                        RedisConnectionFactory redisConnectionFactory,
                        RedisSerializer<Object> redisValueSerializer) {
                    // 在这里构建你自己的 RedisCacheManager 子类
                    return new CustomRedisCacheManager(redisConnectionFactory, redisValueSerializer);
                }

                @Override
                public RedisCacheConfiguration createConfiguration(
                        CacheProperties cacheProperties,
                        VeloxCacheProperties veloxCacheProperties,
                        RedisSerializer<Object> redisValueSerializer) {
                    return RedisCacheConfiguration.defaultCacheConfig();
                }
            }
    );
}
```

如果自定义 registration 与内建 registration 使用相同的 type 名称，自定义实现会覆盖内建默认实现。

## 主要公开类型

- `com.velox.framework.redis.api.manager.VeloxRedisManager`
- `com.velox.framework.redis.api.manager.VeloxRedisCacheManager`
- `com.velox.framework.redis.spi.redis.RedisTemplateCreator`
- `com.velox.framework.redis.spi.redis.RedisTemplateRegistration`
- `com.velox.framework.redis.spi.cache.RedisCacheManagerCreator`
- `com.velox.framework.redis.spi.cache.RedisCacheManagerRegistration`

## 自动装配入口

- `com.velox.framework.redis.autoconfigure.VeloxRedisAutoConfiguration`
- `com.velox.framework.redis.autoconfigure.VeloxCacheAutoConfiguration`
