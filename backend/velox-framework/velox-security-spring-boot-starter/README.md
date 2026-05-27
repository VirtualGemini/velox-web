# Velox Security Spring Boot Starter

Infrastructure-only security starter for Velox.

## Scope

This module owns only:

- token runtime and provider selection
- session facade
- permission SPI and authorization service
- security annotations and aspect
- password hashing and upgrade behavior

This module does not own:

- login policy
- captcha policy
- verification-code policy
- account login methods
- MFA workflow
- email rebind workflow

Those business policies belong to `velox-system`.

## Configuration

Supported properties are limited to infrastructure concerns:

```yaml
velox:
  security:
    swagger-public-enabled: false
    password:
      algorithm: bcrypt
      upgrade-on-login: true
    token:
      mode: stateful
      token-name: Authorization
      timeout: 86400
```

## Notes

- Do not extend `SecuritySessionService` into a business login service.
- Starter-to-starter dependencies inside `velox-framework` are forbidden.
