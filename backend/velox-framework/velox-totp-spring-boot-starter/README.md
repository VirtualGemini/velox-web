# Velox TOTP Spring Boot Starter

Generic TOTP capability starter for Velox.

## Scope

This module provides:

- secret generation
- TOTP verification
- otpauth URI generation
- provisioning model
- SPI-based extension points for secret generation, code generation, URI building, and verification
- disabled/noop behavior when `velox.totp.enabled=false`

This module does not provide:

- MFA enrollment workflow
- MFA challenge orchestration
- recovery-code workflow
- account persistence
- email rebind proof workflow

Those workflows belong to `velox-system`.

## Configuration

```yaml
velox:
  totp:
    enabled: true
    issuer: ${velox.name:Velox}
    digits: 6
    period-seconds: 30
    algorithm: SHA1
    secret-size-bytes: 20
    verify-window-steps: 1
```

## Disabled Behavior

When `velox.totp.enabled=false`, the starter registers `DisabledTotpService`.

- `isEnabled()` returns `false`
- provisioning and verify operations reject usage
- business modules can still inject `TotpService` and degrade explicitly

## Extension Modes

This starter supports three usage modes:

- use the default `TotpService` directly
- override one or more SPI beans while reusing the default `TotpService`
- replace the whole `TotpService`

Available SPI interfaces:

- `TotpSecretGenerator`
- `TotpCodeGenerator`
- `TotpUriBuilder`
- `TotpVerifier`

## API Surface

Typical usage:

```java
TotpProvisioning provisioning = totpService.provision("user@example.com");
TotpVerifyResult result = totpService.verify(secret, code);
```
