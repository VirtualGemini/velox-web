<h2 align="center" id="top">Velox File Spring Boot Starter</h2>
<p align="center">一个独立、可热插拔的文件能力 starter，聚焦存储抽象、provider 扩展、显式 disabled/noop 行为，以及 Spring Boot 自动装配。</p>
<div align="center"><a href="./README.md">English</a> | 简体中文</div>

<br />

## 概览

`velox-file-spring-boot-starter` 是一个面向 Spring Boot 的独立文件能力模块。
它提供统一的文件客户端契约、provider 注册 SPI、内建的 local/FTP/SFTP/S3 实现，以及显式的 disabled 模式。

该 starter 的设计重点有四个：

- 为业务模块提供稳定的文件客户端 API
- 通过 SPI 暴露存储 provider 扩展点
- 在 `velox.file.enabled=false` 时提供显式 noop 行为
- 通过自动装配实现可插拔能力，不依赖 `system` 或 `infra`

## 架构分层

该模块按 email-grade starter 方式组织：

- `api`：面向使用方的公开契约，如 `FileClient`、`FileClientFactory`
- `spi`：官方支持的扩展缝，如 `FileClientManager`、`AbstractFileClient`、`FileClientTypeRegistration`
- `core`：默认管理器实现
- `support`：内建 provider、类型注册表、工具类
- `noop`：disabled 模式下的管理器和客户端
- `exception` 与 `common`：模块内统一的异常、错误码、消息常量
- `autoconfigure` 与 `properties`：Spring Boot 自动装配入口与配置属性

## 内建存储类型

starter 默认注册以下存储类型：

- `10`：本地文件系统
- `11`：FTP
- `12`：SFTP
- `20`：兼容 S3 的对象存储

如果要接入额外存储类型，只需提供一个 `FileClientTypeRegistration` Bean。
如果使用相同 storage code，自定义注册会覆盖内建默认实现。

## Disabled 模式

配置如下：

```yaml
velox:
  file:
    enabled: false
```

关闭后：

- `FileClientFactory` 仍然会注册，注入路径不塌陷
- `FileClientManager` 仍然存在，可继续承担配置解析和运行期管理职责
- 返回的客户端是显式 noop 实现
- 运行时调用会抛出 `FileClientException`
- 能力失效是可观察、可诊断的，而不是静默丢失

## 扩展示例

注册一个自定义数据库 provider：

```java
@Bean
FileClientTypeRegistration dbFileClientTypeRegistration(ApplicationContext applicationContext) {
    return new FileClientTypeRegistration(
            1,
            DbFileClientConfig.class,
            (configId, config) -> new DbFileClient(configId, (DbFileClientConfig) config, applicationContext)
    );
}
```

如果希望复用默认生命周期，可以继承 `AbstractFileClient`。
如果希望完全替换实现，也可以直接返回任意 `FileClient` 实现。

## 主要公开类型

- `com.velox.framework.file.api.client.FileClient`
- `com.velox.framework.file.api.client.FileClientFactory`
- `com.velox.framework.file.spi.client.FileClientManager`
- `com.velox.framework.file.spi.client.AbstractFileClient`
- `com.velox.framework.file.spi.client.FileClientTypeRegistration`

## 自动装配入口

- `com.velox.framework.file.autoconfigure.VeloxFileAutoConfiguration`
