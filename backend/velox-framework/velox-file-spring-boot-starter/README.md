<h2 align="center" id="top">Velox File Spring Boot Starter</h2>
<p align="center">A standalone, hot-pluggable file starter focused on storage abstraction, provider extensibility, explicit disabled-mode behavior, and Spring Boot auto-configuration.</p>
<div align="center">English | <a href="./README.zh-CN.md">简体中文</a></div>

<br />

## Overview

`velox-file-spring-boot-starter` is an independent file capability module for Spring Boot applications.
It provides a unified file client contract, provider registration SPI, built-in local/FTP/SFTP/S3 implementations, and explicit disabled-mode behavior.

The starter is designed around four core goals:

- stable file client API for business modules
- provider SPI for storage-specific extensions
- explicit noop behavior when `velox.file.enabled=false`
- hot-pluggable auto-configuration without `system` or `infra` coupling

## Architecture

The module follows an email-grade starter layout:

- `api`: public contracts such as `FileClient` and `FileClientFactory`
- `spi`: supported extension seams such as `FileClientManager`, `AbstractFileClient`, and `FileClientTypeRegistration`
- `core`: default manager implementation
- `support`: built-in providers, type registry, and utility helpers
- `noop`: disabled-mode manager and client
- `exception` and `common`: starter-owned errors, codes, and reusable messages
- `autoconfigure` and `properties`: Spring Boot entry point and capability switches

## Built-In Providers

The starter currently registers these storage types by default:

- `10`: local filesystem
- `11`: FTP
- `12`: SFTP
- `20`: S3-compatible object storage

Additional providers can be contributed by publishing a Spring bean of type `FileClientTypeRegistration`.
Custom registrations override built-in defaults for the same storage code.

## Disabled Mode

Set:

```yaml
velox:
  file:
    enabled: false
```

When disabled:

- `FileClientFactory` still exists and remains injectable
- `FileClientManager` still exists for configuration parsing and runtime management
- returned clients become explicit noop carriers
- runtime calls fail with `FileClientException`
- failure is intentional and observable instead of silently disappearing

## Extension Example

Register a custom provider:

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

If you want to preserve the default lifecycle path, you can also extend `AbstractFileClient`.
If you want to fully replace the behavior, you can return any plain `FileClient` implementation from the registration.

## Primary Public Types

- `com.velox.framework.file.api.client.FileClient`
- `com.velox.framework.file.api.client.FileClientFactory`
- `com.velox.framework.file.spi.client.FileClientManager`
- `com.velox.framework.file.spi.client.AbstractFileClient`
- `com.velox.framework.file.spi.client.FileClientTypeRegistration`

## Auto-Configuration Entry

- `com.velox.framework.file.autoconfigure.VeloxFileAutoConfiguration`
