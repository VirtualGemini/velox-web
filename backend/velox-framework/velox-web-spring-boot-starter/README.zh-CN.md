# Velox Web Spring Boot Starter

Velox 的必选 Web 能力 starter，负责 API 前缀装配、请求 TraceId、请求时区上下文和请求日志基础能力。该模块不提供 `enabled=false` 或 `noop` 模式，而是通过可替换 SPI 保证“默认可用、需要时可完全重写”。

## 核心特性

- 启动即提供必选 Web 运行时能力，不依赖 `noop`
- 保持现有 `velox.*` 配置键和下游调用方式兼容
- 支持开发者替换 API 前缀解析、TraceId 解析、请求时区解析、请求日志处理，以及整块 MVC 装配
- 保留 `VeloxProperties`、`RequestDateTimeFormatter`、`RequestTimeZoneContext`、`TraceIdFilter`、`RequestLogInterceptor` 等既有入口

## 包结构

- `api`：稳定的路径、TraceId、时区访问能力
- `spi`：对外开放的扩展点
- `core`：默认实现
- `support`：内部 servlet 载体与辅助对象
- `properties`：starter 自有配置结构
- `autoconfigure`：Spring Boot 自动装配
- `common`、`exception`：starter 自有消息与异常

## 配置

当前继续沿用既有模板配置：

```yaml
velox:
  name: velox
  version: 1.0.0
  api-prefix: /api
  web:
    cors:
      allowed-origin-patterns:
        - "http://localhost:*"
        - "http://127.0.0.1:*"
```

该模块是必选模块，不提供关闭开关。如果需要改变行为，应替换对应 SPI bean，或直接提供自己的 `VeloxWebMvcConfigurer`。

## 扩展点

可按需替换以下核心接口：

- `com.velox.framework.web.spi.path.ApiPathPrefixResolver`
- `com.velox.framework.web.spi.tracing.TraceIdResolver`
- `com.velox.framework.web.spi.timezone.RequestTimeZoneResolver`
- `com.velox.framework.web.spi.logging.RequestLogHandler`
- `com.velox.framework.web.spi.mvc.VeloxWebMvcConfigurer`
- `com.velox.framework.web.spi.servlet.TraceIdFilterRegistrationCustomizer`
- `com.velox.framework.web.spi.servlet.RequestTimeZoneFilterRegistrationCustomizer`

如果要直接覆盖 servlet 载体，也可以重写：

- `com.velox.framework.log.RequestLogInterceptor`
- `com.velox.framework.web.TraceIdFilter`
- `com.velox.framework.web.RequestTimeZoneFilter`

## 扩展方式

### 直接实现 SPI

当你要完整替换某个局部契约时，直接实现接口即可。

```java
@Bean
TraceIdResolver traceIdResolver() {
    return request -> request.getHeader("X-Biz-Trace-Id");
}
```

### 继承抽象基类

当你希望保留默认处理链，只改其中一小段行为时，优先继承抽象基类。

```java
@Bean
TraceIdFilter traceIdFilter() {
    return new TraceIdFilter(new DefaultTraceIdResolver()) {
        @Override
        protected void writeTraceId(HttpServletResponse response, String traceId) {
            response.setHeader("X-Biz-Trace-Id", traceId);
            super.writeTraceId(response, traceId);
        }
    };
}
```

### 替换 Filter 注册策略

当 filter 本身不需要改，只想调整注册顺序或 URL pattern 时，替换注册策略即可。

```java
@Bean
TraceIdFilterRegistrationCustomizer traceIdFilterRegistrationCustomizer() {
    return filter -> {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/open-api/*");
        registration.setOrder(50);
        registration.setName("traceIdFilter");
        return registration;
    };
}
```
