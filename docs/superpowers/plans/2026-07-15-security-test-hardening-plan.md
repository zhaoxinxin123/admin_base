# Security And Test Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix findings 2, 4, 5, 6, 7, 8, and 9 while keeping all integration traffic on remote MySQL/Redis at `192.168.3.3` and preventing destructive access to `admin_base`.

**Architecture:** Destructive tests move to guarded database `admin_base_it`; API authorization is driven by seed authorities; OAuth2 uses the production decoder and principal mapping against a test OIDC/JWKS server; domain invariants live in service implementations.

**Tech Stack:** Java 17, Spring Boot 3.5, JUnit 5, Mockito, MockMvc, Spring Security OAuth2 Resource Server, Nimbus JOSE JWT, MySQL 8, Redis.

## Global Constraints

- Do not start local MySQL or Redis.
- Destructive SQL may execute only when the JDBC database name ends with `_it`.
- Use `JsonResponse` and existing `BusinessException` conventions.
- Every protected API must have an explicit `@PreAuthorize` authority expression.
- Production behavior changes require a failing test before implementation.

---

### Task 1: Guard And Isolate Destructive Integration Tests

**Files:**
- Create: `src/test/java/com/admin/base/support/DestructiveTestDatabaseGuard.java`
- Create: `src/test/java/com/admin/base/support/DestructiveTestDatabaseGuardTest.java`
- Modify: `src/test/java/com/admin/base/support/DevRemoteIntegrationTest.java`
- Modify: `src/test/java/com/admin/base/controller/FullApiAuthorizationIntegrationTest.java`
- Modify: `docs/testing/dev-test-process.md`

**Interfaces:**
- Produces: `DestructiveTestDatabaseGuard.requireIsolatedDatabase(String jdbcUrl)`.
- Produces: destructive-test JDBC default `jdbc:mysql://192.168.3.3:3306/admin_base_it?...`.

- [ ] Write tests proving `admin_base` and malformed URLs are rejected while `admin_base_it` is accepted.
- [ ] Run `mvn test -Dtest=DestructiveTestDatabaseGuardTest` and confirm the new class is missing/failing.
- [ ] Implement URL parsing with `java.net.URI`-independent JDBC path parsing and an `_it` suffix check.
- [ ] Invoke the guard before any `TRUNCATE`; add a JUnit resource lock for the database reset.
- [ ] Run the focused guard test and compile the full API test.

### Task 2: Enforce File Authorities And Verify Downloads

**Files:**
- Modify: `docs/database/admin-base-seed-v2.sql`
- Modify: `src/main/java/com/admin/base/infrastructure/controller/CommonController.java`
- Modify: `src/test/java/com/admin/base/controller/FullApiAuthorizationIntegrationTest.java`
- Modify: `src/test/java/com/admin/base/database/SchemaSeedConsistencyTest.java`
- Modify: `docs/api/admin-base-openapi.apifox.yaml`

**Interfaces:**
- Produces authorities `sys:file:upload`, `sys:file:download`, `sys:file:delete`.
- `GET /common/download`: requires download authority and delete authority when `delete=true`.
- `GET /common/download/resource2`: requires download authority.
- `POST /common/upload`: requires upload authority.

- [ ] Extend seed-consistency and MockMvc tests to expect the three authorities and deny limited users.
- [ ] Run focused tests and confirm they fail because common endpoints use `isAuthenticated()` and seed lacks authorities.
- [ ] Add seed permissions and administrator mappings; replace authentication-only expressions.
- [ ] Remove catch-all exception swallowing from downloads and convert missing/invalid files to `BusinessException`.
- [ ] Assert response bytes, content type, attachment header, and `delete=true` file removal.
- [ ] Run seed, security, and full API focused tests.

### Task 3: Make OAuth2 Principal And Audit Flow Real

**Files:**
- Modify: `src/main/java/com/admin/base/infrastructure/security/JwtAuthenticationConverterConfig.java`
- Modify: `src/main/java/com/admin/base/infrastructure/aop/LogAspect.java`
- Modify: `src/test/java/com/admin/base/asp/LogAspectSanitizationTest.java`
- Create: `src/test/java/com/admin/base/config/security/JwtAuthenticationConverterConfigTest.java`
- Replace: `src/test/java/com/admin/base/controller/OAuth2ResourceServerTest.java`

**Interfaces:**
- `JwtAuthenticationConverter` uses `OAuth2Properties.usernameClaim()`.
- `LogAspect` consumes `CurrentUserProvider.currentUser().username()` for both JWT and OAuth2.
- OAuth2 integration test serves local OIDC discovery and JWKS and signs tokens with Nimbus RSA support.

- [ ] Write a converter test expecting `preferred_username` to become `Authentication#getName`.
- [ ] Write a log-aspect test using a non-`UserDetailsImpl` current user.
- [ ] Run both tests and observe failures from missing principal mapping and concrete-principal coupling.
- [ ] Apply `setPrincipalClaimName` and inject `CurrentUserProvider` into the aspect.
- [ ] Replace mocked decoder tests with signed-token tests for valid token, wrong audience, wrong signature, missing authority, and OAuth2 operation username.
- [ ] Run the complete OAuth2 focused suite.

### Task 4: Verify Exact Endpoint Permission Matrix

**Files:**
- Create: `src/test/java/com/admin/base/controller/ApiPermissionMatrixTest.java`
- Modify: `src/test/java/com/admin/base/controller/FullApiAuthorizationIntegrationTest.java`
- Modify: `docs/testing/dev-test-process.md`

**Interfaces:**
- Maintains an explicit mapping of all 25 protected controller methods to expected `@PreAuthorize` expressions.

- [ ] Build a parameterized reflection test that fails when a route lacks or changes its expected authority.
- [ ] Add runtime denial cases using a neighboring authority for every protected route.
- [ ] Keep administrator happy paths as the business-success proof; remove file access from the query-only limited user.
- [ ] Run permission matrix and full API suites.

### Task 5: Enforce Permission Tree And Role Invariants

**Files:**
- Create: `src/test/java/com/admin/base/system/permission/service/PermissionsServiceImplTest.java`
- Create: `src/test/java/com/admin/base/system/role/service/RoleServiceImplTest.java`
- Modify: `src/main/java/com/admin/base/system/permission/service/impl/PermissionsServiceImpl.java`
- Modify: `src/main/java/com/admin/base/system/role/repository/RoleRepository.java`
- Modify: `src/main/java/com/admin/base/system/role/service/impl/RoleServiceImpl.java`
- Modify: `src/main/java/com/admin/base/system/role/controller/RoleController.java`
- Modify: `src/test/java/com/admin/base/controller/FullApiAuthorizationIntegrationTest.java`

**Interfaces:**
- `RoleRepository.existsByRoleNameAndRoleIdNot(String roleName, Long roleId)`.
- Role service validates `ROLE_` prefix, maximum length 10, and duplicate names.
- Permission service rejects self/descendant parents and recursively deletes descendants and mappings.

- [ ] Write unit tests for embedded-prefix names, 11-character names, duplicate update, self-parenting, descendant-parenting, and recursive deletion.
- [ ] Run focused tests and verify failures against current implementations.
- [ ] Move role validation into the service and use the excluding-id repository query.
- [ ] Add cycle detection and descendant collection; delete mappings before permission rows in one transaction.
- [ ] Run domain unit tests and full API error scenarios.

### Task 6: Initialize Remote Test Database And Verify Everything

**Files:**
- Modify: `README.md`
- Modify: `AGENTS.md`

- [ ] Create `admin_base_it` on `192.168.3.3` and import schema/seed only if the guard-approved database is selected.
- [ ] Run `mvn test -Dtest=FullApiAuthorizationIntegrationTest`.
- [ ] Run the OAuth2 focused suite.
- [ ] Run `mvn test`.
- [ ] Run `git diff --check` and inspect `git status` to exclude unrelated `.codebase-memory` artifacts.
