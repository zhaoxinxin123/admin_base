# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=BaseApplicationTests

# Run a single test method
mvn test -Dtest=BaseApplicationTests#getCode

# Run the app (default profile: test, port 9999)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Architecture

**Stack:** Java 17, Spring Boot 3.3.0, Maven, MyBatis Plus 3.5.5, MySQL, Redis, Spring Security, Druid

**Context path:** `/admin-api` (port 9999)

### Package layout

```
com.admin.base
├── annotation/     # Custom annotations: @Log, @RequestLogs, @RepeatInvoke
├── asp/            # AOP aspects: logging, request logging, repeat-invoke guard
├── common/         # JsonResponse — unified API response wrapper {code, msg, data}
├── component/      # RedisLock, EntityInit, ResponseInit
├── config/         # Security, CORS, Redis, Druid, MyBatis interceptor, SysConfig
│   └── security/   # SecurityConfig, UserDetailsImpl, UserDetailsServiceImpl
├── constant/       # Enums: ResponseCode, AdminStatus, CacheTimeType, etc.
├── controller/     # REST controllers (common/ + system/)
├── dto/            # request/ and response/ DTOs, separated by domain
├── entity/         # DB entity classes (keys/, system/)
├── exception/      # GlobalException (@ControllerAdvice), BusinessException
├── filter/         # MyTokenFilter (JWT auth), XssHttpServletRequestWrapper
├── manager/        # AsyncManager, ShutdownManager, factory/AsyncFactory
├── mapper/         # MyBatis mapper interfaces
├── service/        # Service interfaces + impl/ subdirectories
└── utils/          # JWT, file, QR, captcha, steganography, HTML escape, UUID, etc.
```

### Auth flow

1. **Login:** `POST /open/login` → validates captcha → returns JWT token (stored in Redis keyed by admin ID)
2. **Request:** `MyTokenFilter` (runs before `UsernamePasswordAuthenticationFilter`) extracts `Authorization: Bearer <token>`, validates JWT, checks Redis for token match (single-login enforcement)
3. **Authorization:** `UserDetailsServiceImpl.loadUserByUsername()` loads Admin → Roles → Permissions. `@PreAuthorize` on controllers checks permission strings
4. **Public endpoints:** `/open/**`, `/druid/**`, `/actuator/**`, `/error` — all bypass auth

### Database layer

- **ORM:** MyBatis Plus with `BaseMapper<T>` — services use `mapper.selectList()`, `mapper.insert()`, etc.
- **Mapper XML:** in `src/main/resources/mapper/*.xml`
- **DB:** MySQL 8.x, configured per profile in `application-{profile}.yml`
- **Key entities:** `Admin`, `Role`, `Permissions`, `AdminRole` (admin-role mapping), `RolePermission` (role-permission mapping), `GlobalConfig`, `OperationLog`

### Key design patterns

- **`JsonResponse`** — all controllers return `JsonResponse.success(data)` or `JsonResponse.error(code, msg)`. Never throw raw exceptions in controllers.
- **`BusinessException`** — wraps a `JsonResponse`; caught by `GlobalException` handler to return proper error JSON.
- **`@RepeatInvoke`** — AOP annotation to prevent duplicate submissions within a configurable interval.
- **`@Log`** — marks methods for operation logging (async via `AsyncManager`).
- **`MybatisInterceptor`** — auto-sets `createTime`/`updateTime` on entities extending `CommonDate`.

### Captcha

Configured via `sysconfig.captchaType` (`math` or `char`). `GET /open/captchaImage` returns uuid + base64 image. Login requires valid code + uuid pair.

### Monitoring

- **Druid:** Dashboard at `/druid/` (login configured in `application-{profile}.yml`)
- **Prometheus:** Metrics exposed via Micrometer at `/actuator/prometheus`

### Word to PDF (optional)

Uses Aspose.Words (commercial lib). See `word2pdf.md` for local jar installation instructions.

### Testing

Tests use JUnit 5 + Mockito + MockMvc with Spring Security test support:

```java
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
```

Use `@WithMockUser(username = "admin", authorities = {"sys:adminList"})` to test secured endpoints. The `setUp()` method in `BaseApplicationTests` applies `springSecurity()` to MockMvc.