# AGENTS.md

This file gives coding agents the project-specific rules for future development in this repository. Follow it together with the user request and the root system/developer instructions.

## Project State

`admin_base` is a Java 17 / Spring Boot 3.5 backend admin template.
## Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=SeedV2LogicCoverageTest

# Run selected classes
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest

# Run schema/seed checks
mvn test -Dtest=SchemaDraftTest,SchemaSeedConsistencyTest,SeedV2LogicCoverageTest

# Run app, default profile is test
mvn spring-boot:run

# Run with explicit profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Default service settings:

- Port: `9999`
- Context path: `/admin-api`
- Default profile: `test`
- Default auth mode: `jwt`

Database scripts:

```text
docs/database/admin-base-schema-v2.sql
docs/database/admin-base-seed-v2.sql
```

For a fresh local database, import schema first and seed second. No migration script is currently tracked on `main`; if a task restores one, update `SchemaSeedConsistencyTest`, README and this file together. README has the user-facing import commands; keep it updated when these scripts change.

## Test Environment Rules

- Integration tests currently use the `dev` profile and are wired by `DevRemoteIntegrationTest` to MySQL and Redis on `192.168.3.3`.
- Do not start local MySQL or Redis for tests unless the user explicitly asks for that.
- Do not silently change tests to use local services or Testcontainers when the task asks for the shared test environment.
- `DevRemoteIntegrationTest` sets short MySQL/Redis timeouts so an unavailable remote test environment fails quickly instead of hanging.
- If a test needs seed data, prefer a transaction-scoped fixture and rollback after the test.
- `FullApiAuthorizationIntegrationTest` reseeds tables before each test and is guarded to run only against a database whose name ends in `_it`; the default is the disposable `admin_base_it` database.
- Before claiming completion, run the relevant focused tests and then `mvn test`.

## Architecture

**Stack:** Java 17, Spring Boot 3.5.x, Maven, Spring Data JPA, Hibernate, MySQL 8.x, Redis, Spring Security, OAuth2 Resource Server, Druid, Actuator/Prometheus.

### Package Layout

```text
com.admin.base
├── auth/                         # Login, captcha, auth DTOs and open endpoints
│   ├── dto/
│   └── controller/               # CaptchaController, LoginController
├── infrastructure/               # Cross-cutting infrastructure
│   ├── aop/                      # @Log, @RequestLogs, @RepeatInvoke and aspects
│   ├── async/                    # Async task management
│   ├── cache/                    # Redis cache and locks
│   ├── config/                   # Spring, Security, JPA, Redis, Druid config
│   ├── controller/               # BaseController, CommonController (MVC controllers)
│   ├── filter/                   # MyTokenFilter, XssHttpServletRequestWrapper
│   ├── security/                 # JWT/OAuth2, current user abstraction, authority mapping
├── shared/                       # Shared API types, constants, exceptions, entity bases, utilities
│   ├── api/
│   ├── constant/
│   ├── entity/                   # AuditableEntity, CommonDate (JPA mapped superclasses)
│   ├── exception/
│   ├── factory/                  # EntityFactory and ResponseFactory object assemblers
│   └── util/
├── system/                       # System management bounded contexts
│   ├── admin/
│   ├── config/
│   ├── log/
│   ├── permission/
│   └── role/
│       ├── controller/           # REST controllers
│       ├── dto/                  # Request/response DTOs
│       ├── entity/               # JPA entities
│       ├── repository/           # Spring Data JPA repositories
│       └── service/              # Service interfaces and implementations
│           └── impl/
└── user/                         # Spring Security user loading and user DTOs
    ├── dto/
    └── service/                  # Service interfaces and implementations
        └── impl/
```

Business code should follow the dependency direction `controller -> service -> repository/entity`. Keep controllers thin, put business rules in service implementations, and keep database access inside repository interfaces.

## Auth Modes

The active mode is controlled by `admin.auth.mode` / `ADMIN_AUTH_MODE`.

### JWT Mode

1. `GET /open/captchaImage` returns uuid + base64 captcha.
2. `POST /open/login` validates captcha and credentials.
3. The app issues a local JWT and stores the active token in Redis.
4. `MyTokenFilter` validates `Authorization: Bearer <token>`.
5. `UserDetailsServiceImpl` loads Admin -> Roles -> Permissions.
6. Controllers use `@PreAuthorize` with permission strings.

### OAuth2 Mode

1. App acts as OAuth2/OIDC Resource Server.
2. `OAuth2ResourceServerSecurityConfig` builds the decoder and validates issuer/audience.
3. `OAuth2AuthorityMapper` maps external claims to Spring authorities.
4. Business code should use `CurrentUserProvider` / `CurrentUser`, not assume a concrete principal type.

OAuth2 setup notes are in `docs/modernization/oauth2-provider-setup.md`.

## Persistence Rules

- Use Spring Data JPA repositories for new code.
- Do not add new MyBatis Mapper/XML code.
- Entity classes live under `system/<module>/entity/` for system modules and should explicitly declare `@Table`, `@Column`, lengths, nullability and index/unique constraints that match SQL.
- Repositories live under `system/<module>/repository/` for system modules.
- Use `AuditableEntity` for tables with `create_time` and `update_time`.
- Schema is managed by SQL under `docs/database/`; Hibernate runs with `ddl-auto=validate`.
- Do not add database foreign keys. Use unique indexes, normal indexes, service-level checks and tests.
- Seed SQL must include audit columns where the table requires them.

Key database files:

```text
docs/database/admin-base-schema-v2.sql
docs/database/admin-base-seed-v2.sql
```

If a database migration script is added back under `docs/database/`, make sure tests reference the exact tracked filename and document the import order.

## API Rules

- Controllers return `JsonResponse.success(...)` or `JsonResponse.error(...)`.
- Do not return raw maps/objects from public controllers unless wrapped in `JsonResponse`.
- Business validation belongs in Service implementations.
- Business failures should throw `BusinessException` with a `ResponseCode`.
- Keep Controller methods thin: validate request DTO, call Service, return response.
- For pagination, use the existing `PageResult` and `BaseController#getDataTable(...)` shape unless changing the API is explicitly required.
- Keep user-facing endpoint examples in README aligned with actual controller mappings, including existing underscore routes such as `/admin_role`, `/role_permission`, `/sys_global_config`, and `/sys_operation_log`.

## Permission Rules

- Every protected endpoint should have an explicit `@PreAuthorize`.
- Permission strings in code must match `perm` values in seed data.
- Menu-level permissions keep the existing compatibility style, such as `sys:adminList`.
- Button-level permissions should use colon-separated names, such as `sys:admin:add`, `sys:admin:edit`, `sys:admin:delete`.
- If adding a module, update seed permissions and role-permission mappings together.
- Add a logic coverage test for the seed-driven permission flow when permissions are changed.

Known follow-up: some existing mutation endpoints still use older permission names. The v2 seed notes that button-level `@PreAuthorize` migration is a later task; do not mix that broad migration into unrelated changes.

## Adding A New Module

Use this checklist for new domain modules:

1. **Database**
   - Add/adjust SQL in `docs/database/`.
   - Include primary key, unique constraints, indexes and audit columns.
   - Update seed data if the module has menu/button permissions.

2. **Entity**
   - Add entity under `system/<module>/entity/` for system modules.
   - Extend `AuditableEntity` when the table has `create_time`/`update_time`.
   - Keep entity fields aligned with v2 SQL.

3. **Repository**
   - Add `JpaRepository` under `system/<module>/repository/`.
   - Prefer derived queries or explicit JPA queries over ad hoc SQL.

4. **DTO**
   - Add request and response DTOs under `system/<module>/dto/`.
   - Use Bean Validation annotations for input constraints.

5. **Service**
   - Add the service interface and implementation under `system/<module>/service/` (implementation in `service/impl/`).
   - Enforce uniqueness, ownership and state transitions in Service.

6. **Controller**
   - Add controller under `system/<module>/controller/`.
   - Use `JsonResponse`, `@Validated`, and `@PreAuthorize`.
   - Use `CurrentUserProvider` or `BaseController#getUserName()` for current user logic.

7. **Tests**
   - Unit test isolated business logic.
   - Use MockMvc for controller/security behavior.
   - Add schema/seed consistency coverage for database and permission changes.
   - Run focused tests and `mvn test` before committing.

8. **Docs**
   - Update `README.md` for user-facing module behavior.
   - Update this file if the module introduces new agent workflow rules.

## Testing Guidelines

Existing useful tests:

- `SchemaDraftTest` and `SchemaSeedConsistencyTest`: SQL/schema/seed checks.
- `SeedV2LogicCoverageTest`: v2 seed core logic and permission-driven endpoint flow.
- `SecurityConfigTest`, `SecurityBoundaryTest`, `OpenEndpointTest`: JWT-mode security behavior.
- `AuthModeSecurityConfigTest`, `OAuth2*Test`, `OAuth2ResourceServerTest`: auth mode and OAuth2 behavior.
- `FullApiAuthorizationIntegrationTest`: dev-profile white-box API coverage for admin, limited user, public endpoints, error paths, upload/download and operation logs.
- `PackageStructureBoundaryTest`, `PersistenceBoundaryTest`, `RemovedFeatureBoundaryTest`: architecture boundaries.

API documentation for Apifox import is tracked at:

```text
docs/api/admin-base-openapi.apifox.yaml
```

The standardized dev test process is tracked at:

```text
docs/testing/dev-test-process.md
```

Common commands:

```bash
# Seed-v2 logic
mvn test -Dtest=SeedV2LogicCoverageTest

# OAuth2 focused suite
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest

# Architecture boundaries
mvn test -Dtest=PackageStructureBoundaryTest,PersistenceBoundaryTest,RemovedFeatureBoundaryTest

# Schema and seed
mvn test -Dtest=SchemaDraftTest,SchemaSeedConsistencyTest,SeedV2LogicCoverageTest

# Full suite
mvn test
```

When a test fails:

- Read the first failing assertion and stack trace.
- Identify whether it is fixture, schema, auth, service logic or environment.
- Fix the root cause; do not relax assertions to make the test pass.

## Code Style

- Keep changes scoped to the requested task.
- Follow existing package naming and response shapes.
- Prefer small services and DTOs over large controller methods.
- Avoid unrelated refactors.
- Add comments only when they clarify non-obvious logic.
- Do not log secrets, passwords, JWTs, captcha answers or OAuth2 tokens.
- Keep production defaults safe; test defaults may be convenient but must be documented.

## Git / PR Workflow

- Do not work directly on `main`.
- Use a `codex/` branch for agent-created work unless the user requests otherwise.
- Before PR or handoff, run:

```bash
git diff --check
mvn test
```
