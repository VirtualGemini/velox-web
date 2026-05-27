# Velox Web Spring Boot Starter

Required web capability starter for Velox. It provides API prefix assembly, request tracing, request time-zone context, and request logging with hot-pluggable SPI seams instead of hard-coded servlet wiring.

## Features

- Auto-configures a required web runtime without `enabled=false` or `noop` mode
- Keeps `velox.*` property binding compatible with the current template
- Supports developer-owned replacement of path prefix resolution, trace id resolution, time-zone resolution, request logging, and the full MVC configurer
- Preserves legacy public entry points such as `VeloxProperties`, `RequestDateTimeFormatter`, `RequestTimeZoneContext`, `TraceIdFilter`, and `RequestLogInterceptor`

## Package Topology

- `api`: stable request-context helpers such as API path, trace id, and time-zone access
- `spi`: supported extension seams for MVC assembly, tracing, logging, and time-zone resolution
- `core`: default starter implementations
- `support`: internal servlet carriers and logging snapshots
- `properties`: starter-owned configuration structure
- `autoconfigure`: Spring Boot wiring
- `common`, `exception`: starter-owned messages and failures

## Configuration

This starter intentionally keeps the existing template keys:

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

The module is required. It does not expose an `enabled` switch and does not provide `noop` carriers. If developers need different behavior, they should replace the relevant SPI bean or provide their own `VeloxWebMvcConfigurer`.

## Extension Seams

Replace these beans when the default behavior is not enough:

- `com.velox.framework.web.spi.path.ApiPathPrefixResolver`
- `com.velox.framework.web.spi.tracing.TraceIdResolver`
- `com.velox.framework.web.spi.timezone.RequestTimeZoneResolver`
- `com.velox.framework.web.spi.logging.RequestLogHandler`
- `com.velox.framework.web.spi.mvc.VeloxWebMvcConfigurer`
- `com.velox.framework.web.spi.servlet.TraceIdFilterRegistrationCustomizer`
- `com.velox.framework.web.spi.servlet.RequestTimeZoneFilterRegistrationCustomizer`

If a project needs to replace the servlet carriers directly, it can also override:

- `com.velox.framework.log.RequestLogInterceptor`
- `com.velox.framework.web.TraceIdFilter`
- `com.velox.framework.web.RequestTimeZoneFilter`

## Extension Patterns

### Implement the SPI Directly

Use this path when you want to replace a focused contract completely.

```java
@Bean
TraceIdResolver traceIdResolver() {
    return request -> request.getHeader("X-Biz-Trace-Id");
}
```

### Extend the Abstract Base

Use this path when you want to keep the default pipeline shape but override selected behavior.

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

### Replace Filter Registration Strategy

Use this path when the filter itself is fine but the registration order or URL mapping should change.

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

## Compatibility

Current downstream code can continue using:

- `com.velox.framework.config.VeloxProperties`
- `com.velox.framework.web.RequestDateTimeFormatter`
- `com.velox.framework.web.RequestTimeZoneContext`
- `com.velox.framework.web.TraceIdFilter`
- `com.velox.framework.log.RequestLogInterceptor`

These classes are now compatibility facades over the new starter architecture, so business modules can migrate gradually without losing behavior.
