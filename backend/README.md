<h2 align="center" id="top">Velox Pro</h2>
<p align="center">Backend implementation for the Velox admin system, organized around layered DDD and pluggable starters.</p>
<div align="center">English | <a href="./README.zh-CN.md">简体中文</a></div>

<br />

## Overview

`velox-pro` is the backend of the `velox` project. It is responsible for authentication, permissions, users, menus, files, mail, and foundational capability wiring.

## Tech Stack

- Java 25
- Spring Boot 3.4.5
- Maven 3.9+
- MyBatis-Plus 3.5.12
- MySQL 8.4.0
- PostgreSQL 18.4.0
- Redis 7
- Sa-Token 1.40.0
- SpringDoc OpenAPI 2.8.6
- Spring Validation
- Spring AOP
- Hutool 5.8.34
- Guava 33.4.8-jre
- EasyCaptcha 1.6.2
- JavaMail
- AWS SDK S3 2.25.0
- Tika 2.9.2
- JSch 0.2.19
- Commons Net 3.12.0

## Modules

```text
velox-dependencies  # Dependency version management
velox-common        # Shared business modules
velox-framework     # Pluggable, reusable, configuration-driven capability starters
velox-infra         # Infrastructure wiring layer
velox-system        # System business modules
velox-server        # Application startup module
```

## Current Features

- Login, logout, registration, captcha, and password recovery
- Current user info, profile, password, and avatar management
- User, role, and menu management
- Role-to-menu permission assignment
- File upload, creation, query, deletion, and batch deletion
- Presigned file upload and download
- File configuration management, enable/disable, primary config switching, and connectivity testing
- File storage support for local, FTP, SFTP, and S3-compatible backends
- Mail sending capability
- Unified response model, exception handling, request logging, trace propagation, and operation logging
- ID generation capability

## Local Development

Requirements:

- JDK 25+
- Maven 3.9+
- MySQL 8 / PostgreSQL 14+
- Redis 7

```bash
cd script/docker
docker compose up -d
```

```bash
mvn clean compile
```

```bash
cd velox-server
mvn spring-boot:run
```

## License

MIT
