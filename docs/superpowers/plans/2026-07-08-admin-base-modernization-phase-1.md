# Admin Base Modernization Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first modernization phase for `admin_base`: stabilize dependencies, configuration, tests, response/error infrastructure, JWT security, MyBatis Plus boundaries, and the MySQL schema draft without migrating to JPA yet.

**Architecture:** Phase 1 keeps the current Spring MVC + Spring Security + MyBatis Plus runtime working while moving framework-specific types behind project-owned interfaces. Public API response shape remains `{code,msg,data}`. JPA, OAuth2 provider integration, and package-by-feature restructuring are deferred to later plans.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Security 6.5.x, MyBatis Plus retained internally, MySQL, Redis, Jackson, JUnit 5, MockMvc, Testcontainers where database/Redis integration is required.

## Global Constraints

- Continue using MySQL.
- Do not create database foreign keys; use primary keys, unique indexes, normal indexes, service checks, and tests.
- Use `spring-boot-starter-parent` for dependency and plugin management.
- Keep Java at 17 unless a separate Java 21 decision is made.
- Keep existing response exterior `{code,msg,data}`.
- Keep existing APIs basically compatible; obvious errors may be fixed with compatibility notes.
- Default auth mode is local JWT; OAuth2/OIDC is only a reserved structure in Phase 1.
- Do not remove the token filter outright; it is currently used by `SecurityConfig.addFilterBefore(...)`.
- Remove `tb_keys`, `tb_records`, keyword-recognition code, Office/PDF code, QR code code, steganography/watermark code, and their dependencies.
- Do not migrate to JPA in Phase 1.
- Add a minimal test safety net before changing core behavior, then add module tests as each module changes.
- Do not include unrelated existing worktree changes in commits.

---

## File Structure Map

Phase 1 creates or modifies these focused units:

- `pom.xml`: parent, dependencies, plugin management, removal of heavy/special-purpose libraries.
- `src/main/resources/application.yml`: safe defaults and auth mode property.
- `src/main/resources/application-dev.yml`, `src/main/resources/application-prod.yml`, `src/main/resources/application-test.yml`: environment-variable based datasource/Redis/JWT configuration.
- `src/main/java/com/admin/base/common/JsonResponse.java`: generic compatible response wrapper.
- `src/main/java/com/admin/base/common/PageQuery.java`: project-owned page request object.
- `src/main/java/com/admin/base/common/PageResult.java`: project-owned page response object.
- `src/main/java/com/admin/base/exception/BusinessException.java`: error code/message exception.
- `src/main/java/com/admin/base/exception/GlobalException.java`: single compatible error response surface.
- `src/main/java/com/admin/base/controller/common/BaseController.java`: remove `IPage` dependency and expose `PageResult` mapping.
- `src/main/java/com/admin/base/config/security/AuthModeProperties.java`: `admin.auth.mode` binding.
- `src/main/java/com/admin/base/config/security/SecurityConfig.java`: JWT-mode security chain and Spring Security 6 method-security cleanup.
- `src/main/java/com/admin/base/filter/MyTokenFilter.java`: refactor into a clearer JWT authentication filter while preserving bean wiring.
- `src/main/java/com/admin/base/service/system/*.java`: remove `IService<T>` inheritance and `IPage` return types from service interfaces.
- `src/main/java/com/admin/base/service/system/impl/*.java`: keep MyBatis Plus implementation internally and adapt to `PageResult<T>`.
- `src/main/java/com/admin/base/controller/system/*.java`: use project-owned page result structures.
- `src/main/java/com/admin/base/entity/keys`, `src/main/java/com/admin/base/dto/request/keys`, `src/main/java/com/admin/base/dto/response/keys`: delete keyword-recognition data models and DTOs.
- `src/main/java/com/admin/base/utils/Boyer`, `src/main/java/com/admin/base/utils/SteganographyImgUtils.java`, `src/main/java/com/admin/base/utils/SteganographyStringUtils.java`: delete special-purpose tooling.
- `docs/database/admin-base-schema-v2.sql`: MySQL DDL draft for retained system tables.
- `docs/database/admin-base-seed-v2.sql`: seed data draft for admin, role, permissions, and global config.
- `src/test/java/com/admin/base/**`: baseline and module tests.

## Task 1: Baseline Test Harness

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application-test.yml`
- Create: `src/test/java/com/admin/base/common/JsonResponseTest.java`
- Create: `src/test/java/com/admin/base/controller/OpenEndpointTest.java`
- Create: `src/test/java/com/admin/base/controller/SecurityBoundaryTest.java`
- Modify: `src/test/java/com/admin/base/BaseApplicationTests.java`

**Interfaces:**
- Consumes: existing `JsonResponse`, `/open/captchaImage`, Spring Security config.
- Produces: a reliable minimal test harness for later tasks.

- [ ] **Step 1: Add test dependencies before changing behavior**

In `pom.xml`, under test dependencies, keep `spring-boot-starter-test` and `spring-security-test`, and add Testcontainers dependencies for later MySQL/Redis integration tests:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Make `application-test.yml` safe**

Replace real credentials in `src/main/resources/application-test.yml` with local test defaults:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${TEST_DATASOURCE_URL:jdbc:mysql://127.0.0.1:3306/admin_base_test?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false}
    username: ${TEST_DATASOURCE_USERNAME:root}
    password: ${TEST_DATASOURCE_PASSWORD:}
  data:
    redis:
      host: ${TEST_REDIS_HOST:127.0.0.1}
      port: ${TEST_REDIS_PORT:6379}
      password: ${TEST_REDIS_PASSWORD:}
admin:
  auth:
    mode: jwt
```

- [ ] **Step 3: Write response-shape baseline test**

Create `src/test/java/com/admin/base/common/JsonResponseTest.java`:

```java
package com.admin.base.common;

import com.admin.base.constant.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResponseTest {

    @Test
    void successKeepsCompatibleShape() {
        JsonResponse response = JsonResponse.success("ok");

        assertThat(response.getCode()).isEqualTo(ResponseCode.CODE_OK);
        assertThat(response.getMsg()).isEqualTo("成功");
        assertThat(response.getData()).isEqualTo("ok");
    }

    @Test
    void errorKeepsCompatibleShape() {
        JsonResponse response = JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "系统繁忙");

        assertThat(response.getCode()).isEqualTo(ResponseCode.CODE_SYS_ERROR);
        assertThat(response.getMsg()).isEqualTo("系统繁忙");
        assertThat(response.getData()).isNull();
    }
}
```

- [ ] **Step 4: Write open endpoint smoke test**

Create `src/test/java/com/admin/base/controller/OpenEndpointTest.java`:

```java
package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void captchaEndpointIsPublicAndCompatible() throws Exception {
        mockMvc.perform(get("/open/captchaImage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists());
    }
}
```

- [ ] **Step 5: Write protected endpoint boundary test**

Create `src/test/java/com/admin/base/controller/SecurityBoundaryTest.java`:

```java
package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityBoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 6: Temporarily disable brittle legacy tests**

In `src/test/java/com/admin/base/BaseApplicationTests.java`, add `@Disabled("Replaced by focused baseline tests during modernization phase 1")` at class level:

```java
@Disabled("Replaced by focused baseline tests during modernization phase 1")
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BaseApplicationTests {
}
```

- [ ] **Step 7: Run baseline tests**

Run:

```bash
mvn test -Dtest=JsonResponseTest,OpenEndpointTest,SecurityBoundaryTest
```

Expected: `JsonResponseTest` passes. If web-context tests fail because real MySQL/Redis is required, keep `JsonResponseTest` passing and record the exact missing external dependency in the task notes before proceeding to Task 2.

- [ ] **Step 8: Commit**

```bash
git add pom.xml src/main/resources/application-test.yml src/test/java/com/admin/base
git commit -m "test: add modernization baseline tests"
```

## Task 2: Parent POM and Dependency Cleanup

**Files:**
- Modify: `pom.xml`

**Interfaces:**
- Consumes: existing Maven project.
- Produces: Spring Boot 3.5.x parent-managed dependency graph and a reduced core dependency set.

- [ ] **Step 1: Switch to Spring Boot parent**

At the top of `pom.xml`, add:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>
    <relativePath/>
</parent>
```

Keep:

```xml
<groupId>com.admin</groupId>
<artifactId>base</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

- [ ] **Step 2: Simplify properties**

Replace existing properties with:

```xml
<properties>
    <java.version>17</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

- [ ] **Step 3: Remove explicit Spring Boot dependency management**

Delete the existing `<dependencyManagement>` section importing `spring-boot-dependencies`.

- [ ] **Step 4: Keep the core starter set**

Ensure the dependency list contains these starter dependencies without explicit versions:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

- [ ] **Step 5: Keep MyBatis Plus internally for Phase 1**

Keep only the Spring Boot 3 MyBatis Plus starter and remove the duplicate starter:

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

Remove `org.mybatis:mybatis-spring`, `com.baomidou:mybatis-plus-boot-starter`, and `com.baomidou:mybatis-plus-generator`.

- [ ] **Step 6: Remove special-purpose tool dependencies**

Delete dependencies for:

```text
org.apache.poi:poi-ooxml
org.apache.poi:poi
org.apache.poi:poi-scratchpad
org.apache.velocity:velocity-engine-core
com.google.zxing:core
com.google.zxing:javase
com.google.code.gson:gson
com.alibaba.fastjson2:fastjson2
javax.annotation:javax.annotation-api
```

- [ ] **Step 7: Replace JWT dependency with maintained JJWT modules**

Remove:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

Add:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 8: Keep test dependencies**

Ensure these test dependencies exist:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 9: Simplify plugins**

Keep only:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

- [ ] **Step 10: Verify dependency graph**

Run:

```bash
mvn -DskipTests dependency:tree
```

Expected: build succeeds and output no longer contains `fastjson2`, `gson`, `poi`, `zxing`, `javax.annotation-api`, or duplicate MyBatis Plus starters.

- [ ] **Step 11: Run tests**

Run:

```bash
mvn test -Dtest=JsonResponseTest
```

Expected: PASS.

- [ ] **Step 12: Commit**

```bash
git add pom.xml
git commit -m "build: simplify spring boot dependency management"
```

## Task 3: Safe Configuration and Auth Mode Properties

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/application-dev.yml`
- Modify: `src/main/resources/application-prod.yml`
- Modify: `src/main/resources/application-test.yml`
- Create: `src/main/java/com/admin/base/config/security/AuthModeProperties.java`
- Modify: `src/main/java/com/admin/base/config/common/SysConfig.java`

**Interfaces:**
- Produces: `AuthModeProperties.mode(): AuthMode` and safe environment-variable based configuration.

- [ ] **Step 1: Add auth mode properties class**

Create `src/main/java/com/admin/base/config/security/AuthModeProperties.java`:

```java
package com.admin.base.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.auth")
public record AuthModeProperties(AuthMode mode) {

    public AuthModeProperties {
        if (mode == null) {
            mode = AuthMode.JWT;
        }
    }

    public enum AuthMode {
        JWT,
        OAUTH2
    }
}
```

- [ ] **Step 2: Register configuration properties**

In `SecurityConfig`, add:

```java
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(AuthModeProperties.class)
```

- [ ] **Step 3: Update base application config**

In `src/main/resources/application.yml`, keep non-sensitive defaults only:

```yaml
spring:
  application:
    name: admin
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:test}
  servlet:
    multipart:
      max-request-size: 30MB
      max-file-size: 30MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
server:
  port: ${SERVER_PORT:9999}
  servlet:
    context-path: /admin-api
admin:
  auth:
    mode: ${ADMIN_AUTH_MODE:jwt}
jwt:
  tokenHead: Bearer
  tokenHeader: Authorization
  tokenPrefix: Bearer
  secret: ${JWT_SECRET:change-me-in-local-env}
  expiration: ${JWT_EXPIRATION_SECONDS:7200}
```

- [ ] **Step 4: Replace dev datasource secrets with environment variables**

In `src/main/resources/application-dev.yml`, use:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    name: adminBase
    url: ${DEV_DATASOURCE_URL:jdbc:mysql://127.0.0.1:3306/admin_base?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false}
    username: ${DEV_DATASOURCE_USERNAME:root}
    password: ${DEV_DATASOURCE_PASSWORD:}
  data:
    redis:
      database: ${DEV_REDIS_DATABASE:0}
      host: ${DEV_REDIS_HOST:127.0.0.1}
      port: ${DEV_REDIS_PORT:6379}
      password: ${DEV_REDIS_PASSWORD:}
```

- [ ] **Step 5: Replace prod datasource secrets with environment variables**

In `src/main/resources/application-prod.yml`, use:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    name: adminBase
    url: ${PROD_DATASOURCE_URL}
    username: ${PROD_DATASOURCE_USERNAME}
    password: ${PROD_DATASOURCE_PASSWORD}
  data:
    redis:
      database: ${PROD_REDIS_DATABASE:0}
      host: ${PROD_REDIS_HOST}
      port: ${PROD_REDIS_PORT:6379}
      password: ${PROD_REDIS_PASSWORD:}
```

- [ ] **Step 6: Keep MyBatis Plus config until Phase 2**

In each active profile, keep:

```yaml
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

- [ ] **Step 7: Verify no committed secrets remain in YAML**

Run:

```bash
rg -n "password: [^$]|zxx|hjdz|10086|192\\.168|172\\.16" src/main/resources/application*.yml
```

Expected: no output for hardcoded secrets or private infrastructure addresses.

- [ ] **Step 8: Run config binding test**

Create `src/test/java/com/admin/base/config/AuthModePropertiesTest.java`:

```java
package com.admin.base.config;

import com.admin.base.config.security.AuthModeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class AuthModePropertiesTest {

    @Test
    void defaultsToJwtWhenModeIsAbsent() {
        AuthModeProperties properties = new AuthModeProperties(null);

        assertThat(properties.mode()).isEqualTo(AuthModeProperties.AuthMode.JWT);
    }

    @Test
    void bindsOauth2Mode() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("admin.auth.mode", "oauth2");

        AuthModeProperties properties = Binder.get(environment)
                .bind("admin.auth", Bindable.of(AuthModeProperties.class))
                .orElseThrow();

        assertThat(properties.mode()).isEqualTo(AuthModeProperties.AuthMode.OAUTH2);
    }
}
```

Run:

```bash
mvn test -Dtest=AuthModePropertiesTest
```

Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add src/main/resources/application*.yml src/main/java/com/admin/base/config/security/AuthModeProperties.java src/test/java/com/admin/base/config/AuthModePropertiesTest.java
git commit -m "chore: externalize runtime configuration"
```

## Task 4: Compatible Response, Page, and Exception Infrastructure

**Files:**
- Modify: `src/main/java/com/admin/base/common/JsonResponse.java`
- Create: `src/main/java/com/admin/base/common/PageQuery.java`
- Create: `src/main/java/com/admin/base/common/PageResult.java`
- Modify: `src/main/java/com/admin/base/exception/BusinessException.java`
- Modify: `src/main/java/com/admin/base/exception/GlobalException.java`
- Modify: `src/main/java/com/admin/base/controller/common/BaseController.java`
- Create: `src/test/java/com/admin/base/common/PageResultTest.java`
- Create: `src/test/java/com/admin/base/exception/BusinessExceptionTest.java`

**Interfaces:**
- Produces: `JsonResponse<T>`, `PageQuery`, `PageResult<T>`, `BusinessException(int code, String message)`.

- [ ] **Step 1: Write failing generic response test**

Extend `JsonResponseTest`:

```java
@Test
void genericResponseCarriesTypedData() {
    JsonResponse<String> response = JsonResponse.success("typed");

    assertThat(response.getData()).isEqualTo("typed");
}
```

Run:

```bash
mvn test -Dtest=JsonResponseTest
```

Expected before implementation: compile fails because `JsonResponse` is not generic.

- [ ] **Step 2: Make `JsonResponse` generic**

Replace `JsonResponse` with:

```java
package com.admin.base.common;

import com.admin.base.constant.ResponseCode;
import lombok.Data;

@Data
public class JsonResponse<T> {

    private Integer code;
    private String msg;
    private T data;

    public static JsonResponse<Void> success() {
        JsonResponse<Void> response = new JsonResponse<>();
        response.setCode(ResponseCode.CODE_OK);
        response.setMsg("成功");
        return response;
    }

    public static <T> JsonResponse<T> success(T data) {
        JsonResponse<T> response = new JsonResponse<>();
        response.setCode(ResponseCode.CODE_OK);
        response.setMsg("成功");
        response.setData(data);
        return response;
    }

    public static <T> JsonResponse<T> success(T data, String msg) {
        JsonResponse<T> response = new JsonResponse<>();
        response.setCode(ResponseCode.CODE_OK);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    public static JsonResponse<Void> error(Integer code, String msg) {
        JsonResponse<Void> response = new JsonResponse<>();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
}
```

- [ ] **Step 3: Add project-owned pagination types**

Create `src/main/java/com/admin/base/common/PageQuery.java`:

```java
package com.admin.base.common;

public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
    }

    public long offset() {
        return (long) (page - 1) * size;
    }
}
```

Create `src/main/java/com/admin/base/common/PageResult.java`:

```java
package com.admin.base.common;

import java.util.List;

public record PageResult<T>(List<T> rows, long total, int page, int size) {

    public PageResult {
        rows = rows == null ? List.of() : List.copyOf(rows);
        if (total < 0) {
            total = 0;
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = rows.size();
        }
    }
}
```

- [ ] **Step 4: Add page result test**

Create `src/test/java/com/admin/base/common/PageResultTest.java`:

```java
package com.admin.base.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void exposesRowsAndTotalForLegacyTableShape() {
        PageResult<String> result = new PageResult<>(List.of("a", "b"), 12, 2, 2);

        assertThat(result.rows()).containsExactly("a", "b");
        assertThat(result.total()).isEqualTo(12);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
    }
}
```

- [ ] **Step 5: Refactor `BusinessException`**

Replace `BusinessException` with:

```java
package com.admin.base.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 6: Refactor `GlobalException`**

Use:

```java
@Slf4j
@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public JsonResponse<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied", e);
        return JsonResponse.error(ResponseCode.CODE_TOKEN_ERROR, "没有权限访问");
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public JsonResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return JsonResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public JsonResponse<Void> handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("参数错误");
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, message);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public JsonResponse<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed", e);
        return JsonResponse.error(ResponseCode.CODE_NO_LOGIN, "请先登录");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResponse<Void> handleGlobal(Exception e) {
        log.error("Unhandled exception", e);
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "系统繁忙");
    }
}
```

Add imports:

```java
import com.admin.base.common.JsonResponse;
import com.admin.base.constant.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
```

- [ ] **Step 7: Remove `IPage` from `BaseController` output helpers**

Replace `getDataTable(IPage<?>...)` with:

```java
protected Map<String, Object> getDataTable(PageResult<?> pageResult, int dataMapInitialCapacity) {
    Map<String, Object> data = new HashMap<>(dataMapInitialCapacity);
    data.put("rows", pageResult.rows());
    data.put("total", pageResult.total());
    return data;
}
```

Remove `com.baomidou.mybatisplus.core.metadata.IPage` import and add `com.admin.base.common.PageResult`.

- [ ] **Step 8: Run focused tests**

Run:

```bash
mvn test -Dtest=JsonResponseTest,PageResultTest,BusinessExceptionTest
```

Expected: PASS after updating all compile errors caused by `BusinessException` constructor changes.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/admin/base/common src/main/java/com/admin/base/exception src/main/java/com/admin/base/controller/common/BaseController.java src/test/java/com/admin/base/common src/test/java/com/admin/base/exception
git commit -m "refactor: standardize response and exception infrastructure"
```

## Task 5: JWT Security Filter Governance

**Files:**
- Modify: `src/main/java/com/admin/base/config/security/SecurityConfig.java`
- Modify: `src/main/java/com/admin/base/filter/MyTokenFilter.java`
- Modify: `src/main/java/com/admin/base/config/CorsConfig.java`
- Modify: `src/main/java/com/admin/base/utils/JwtTokenUtil.java`
- Create: `src/test/java/com/admin/base/config/security/SecurityConfigTest.java`

**Interfaces:**
- Consumes: `AuthModeProperties`, `JsonResponse<Void>`.
- Produces: JWT mode security chain, reserved auth mode property, filter using Jackson.

- [ ] **Step 1: Update method security annotation**

In `SecurityConfig`, replace:

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
```

with:

```java
@EnableMethodSecurity
```

Add import:

```java
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
```

- [ ] **Step 2: Convert token filter to once-per-request style**

Change class declaration:

```java
public class MyTokenFilter extends OncePerRequestFilter {
}
```

Replace `doFilter(...)` with:

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    String authHeader = request.getHeader(this.tokenHeader);
    if (authHeader == null || !authHeader.startsWith(this.tokenHead)) {
        filterChain.doFilter(request, response);
        return;
    }

    String authToken = authHeader.substring(this.tokenHead.length()).trim();
    String username = jwtTokenUtil.getUserNameFromToken(authToken);
    if (username == null) {
        writeError(response, ResponseCode.CODE_TOKEN_ERROR, "token失效,请重新登录");
        return;
    }

    UserDetailsImpl userDetail = (UserDetailsImpl) jwtTokenUtil.getUserDetail(authToken);
    String cachedToken = cacheService.getTokenById(userDetail.getAdminId().toString());
    if (StringUtils.isBlank(cachedToken) || !cachedToken.equals(authToken)) {
        writeError(response, ResponseCode.CODE_NO_LOGIN, "token失效,请重新登录");
        return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = jwtTokenUtil.getUserDetail(authToken);
        if (jwtTokenUtil.validateToken(authToken, userDetails)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
            authentication.setDetails(userDetails);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    filterChain.doFilter(request, response);
}
```

- [ ] **Step 3: Inject Jackson `ObjectMapper`**

Add field:

```java
@Resource
private ObjectMapper objectMapper;
```

Add method:

```java
private void writeError(HttpServletResponse response, Integer code, String message) throws IOException {
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().println(objectMapper.writeValueAsString(JsonResponse.error(code, message)));
    response.getWriter().flush();
}
```

Imports:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
```

Remove `Gson` usage.

- [ ] **Step 4: Move CORS out of filter**

Remove `setHead(...)` from `MyTokenFilter`. In `CorsConfig`, provide:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

- [ ] **Step 5: Keep JWT filter bean only in JWT mode**

If `AuthModeProperties` is available, add:

```java
@Bean
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "jwt", matchIfMissing = true)
public MyTokenFilter myTokenFilter() {
    return new MyTokenFilter();
}
```

Add import:

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
```

- [ ] **Step 6: Keep OAuth2 mode reserved but not implemented**

Add a comment in `SecurityConfig` near `filterChain`:

```java
// OAuth2/OIDC mode is reserved for a later implementation plan.
// Phase 1 keeps JWT mode as the only executable security chain.
```

- [ ] **Step 7: Run security tests**

Run:

```bash
mvn test -Dtest=SecurityBoundaryTest,SecurityConfigTest
```

Expected: protected endpoints still reject anonymous requests, and `/open/**` remains public.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/admin/base/config/security src/main/java/com/admin/base/filter/MyTokenFilter.java src/main/java/com/admin/base/config/CorsConfig.java src/main/java/com/admin/base/utils/JwtTokenUtil.java src/test/java/com/admin/base/config/security
git commit -m "refactor: govern jwt security filter"
```

## Task 6: Service Contract Decoupling from MyBatis Plus

**Files:**
- Modify: `src/main/java/com/admin/base/service/system/IAdminService.java`
- Modify: `src/main/java/com/admin/base/service/system/IRoleService.java`
- Modify: `src/main/java/com/admin/base/service/system/IGlobalConfigService.java`
- Modify: `src/main/java/com/admin/base/service/system/IOperationLogService.java`
- Modify: `src/main/java/com/admin/base/service/system/IPermissionsService.java`
- Modify: `src/main/java/com/admin/base/service/system/IAdminRoleService.java`
- Modify: `src/main/java/com/admin/base/service/system/IRolePermissionService.java`
- Modify: `src/main/java/com/admin/base/service/system/impl/*.java`
- Modify: `src/main/java/com/admin/base/controller/system/*.java`
- Create: `src/test/java/com/admin/base/service/system/ServiceContractTest.java`

**Interfaces:**
- Consumes: `PageResult<T>`.
- Produces: service interfaces without `IService<T>` inheritance or `IPage` return types.

- [ ] **Step 1: Write reflection test that rejects MyBatis Plus service contracts**

Create `src/test/java/com/admin/base/service/system/ServiceContractTest.java`:

```java
package com.admin.base.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceContractTest {

    private final List<Class<?>> serviceTypes = List.of(
            IAdminService.class,
            IAdminRoleService.class,
            IGlobalConfigService.class,
            IOperationLogService.class,
            IPermissionsService.class,
            IRolePermissionService.class,
            IRoleService.class
    );

    @Test
    void systemServiceInterfacesDoNotExtendMybatisPlusIService() {
        for (Class<?> serviceType : serviceTypes) {
            assertThat(IService.class.isAssignableFrom(serviceType))
                    .as(serviceType.getName())
                    .isFalse();
        }
    }

    @Test
    void systemServiceInterfacesDoNotExposeIPage() {
        for (Class<?> serviceType : serviceTypes) {
            for (Method method : serviceType.getMethods()) {
                assertThat(method.getReturnType())
                        .as(serviceType.getSimpleName() + "." + method.getName())
                        .isNotEqualTo(IPage.class);
            }
        }
    }
}
```

Run:

```bash
mvn test -Dtest=ServiceContractTest
```

Expected before implementation: FAIL.

- [ ] **Step 2: Remove `IService<T>` inheritance from service interfaces**

For each service interface, remove:

```java
extends IService<EntityType>
```

Remove imports:

```java
import com.baomidou.mybatisplus.extension.service.IService;
```

- [ ] **Step 3: Replace page return types**

In `IAdminService`, replace:

```java
IPage<Admin> getAdminList(Integer page, Integer size, String username);
```

with:

```java
PageResult<Admin> getAdminList(Integer page, Integer size, String username);
```

In `IGlobalConfigService`, replace:

```java
IPage<GlobalConfig> selectByPage(Integer page, Integer size, String key, String note);
```

with:

```java
PageResult<GlobalConfig> selectByPage(Integer page, Integer size, String key, String note);
```

In `IOperationLogService`, replace:

```java
IPage<OperationLog> listPage(OperationLogListParam operationListParam);
```

with:

```java
PageResult<OperationLog> listPage(OperationLogListParam operationListParam);
```

In `IRoleService`, replace:

```java
IPage<Role> getRolePage(Integer page, Integer size);
```

with:

```java
PageResult<Role> getRolePage(Integer page, Integer size);
```

Add import:

```java
import com.admin.base.common.PageResult;
```

- [ ] **Step 4: Keep MyBatis Plus implementation internal**

In each implementation method, adapt MyBatis `IPage<T>` to `PageResult<T>`:

```java
Page<T> pageRequest = new Page<>(page, size);
IPage<T> pageResult = this.baseMapper.selectPage(pageRequest, queryWrapper);
return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
```

- [ ] **Step 5: Update controllers**

In controllers currently doing:

```java
IPage<Admin> adminList = iAdminService.getAdminList(...);
Map<String, Object> dataTable = getDataTable(adminList, 2);
```

Use:

```java
PageResult<Admin> adminList = iAdminService.getAdminList(...);
Map<String, Object> dataTable = getDataTable(adminList, 2);
```

Remove MyBatis Plus `IPage` imports from controllers.

- [ ] **Step 6: Run contract test**

Run:

```bash
mvn test -Dtest=ServiceContractTest
```

Expected: PASS.

- [ ] **Step 7: Run affected controller compile test**

Run:

```bash
mvn test -DskipTests
```

Expected: compile succeeds.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/admin/base/service/system src/main/java/com/admin/base/service/system/impl src/main/java/com/admin/base/controller/system src/test/java/com/admin/base/service/system/ServiceContractTest.java
git commit -m "refactor: hide mybatis plus from service contracts"
```

## Task 7: Remove Keys, Records, and Special-Purpose Tools

**Files:**
- Delete: `src/main/java/com/admin/base/entity/keys/`
- Delete: `src/main/java/com/admin/base/dto/request/keys/`
- Delete: `src/main/java/com/admin/base/dto/response/keys/`
- Delete: `src/main/java/com/admin/base/utils/Boyer/`
- Delete: `src/main/java/com/admin/base/utils/SteganographyImgUtils.java`
- Delete: `src/main/java/com/admin/base/utils/SteganographyStringUtils.java`
- Modify: `src/main/resources/base_sql_v1.0.sql`
- Modify: `pom.xml`
- Create: `src/test/java/com/admin/base/architecture/RemovedFeatureBoundaryTest.java`

**Interfaces:**
- Produces: no compile-time references to removed keyword/Office/PDF/QR/steganography features.

- [ ] **Step 1: Write boundary test for removed packages**

Create `src/test/java/com/admin/base/architecture/RemovedFeatureBoundaryTest.java`:

```java
package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RemovedFeatureBoundaryTest {

    @Test
    void removedFeaturePackagesDoNotExist() {
        List<Path> removedPaths = List.of(
                Path.of("src/main/java/com/admin/base/entity/keys"),
                Path.of("src/main/java/com/admin/base/dto/request/keys"),
                Path.of("src/main/java/com/admin/base/dto/response/keys"),
                Path.of("src/main/java/com/admin/base/utils/Boyer")
        );

        for (Path path : removedPaths) {
            assertThat(Files.exists(path)).as(path.toString()).isFalse();
        }
    }
}
```

- [ ] **Step 2: Delete key and record Java packages**

Run:

```bash
git rm -r src/main/java/com/admin/base/entity/keys
git rm -r src/main/java/com/admin/base/dto/request/keys
git rm -r src/main/java/com/admin/base/dto/response/keys
```

- [ ] **Step 3: Delete special-purpose utility files**

Run:

```bash
git rm -r src/main/java/com/admin/base/utils/Boyer
git rm src/main/java/com/admin/base/utils/SteganographyImgUtils.java
git rm src/main/java/com/admin/base/utils/SteganographyStringUtils.java
```

- [ ] **Step 4: Search for remaining references**

Run:

```bash
rg -n "entity\\.keys|dto\\.request\\.keys|dto\\.response\\.keys|Steganography|Boyer|tb_keys|tb_records|RecordsResponse|KeysResponse" src/main/java src/main/resources pom.xml
```

Expected: no output except lines in `base_sql_v1.0.sql` if historical SQL still contains the removed tables.

- [ ] **Step 5: Remove removed table DDL from active schema scripts**

If `base_sql_v1.0.sql` contains `tb_keys` or `tb_records`, delete those table sections and their insert data from active schema initialization. Keep the old file only if it is treated as historical documentation and add a header comment:

```sql
-- Historical bootstrap script. New schema lives in docs/database/admin-base-schema-v2.sql.
```

- [ ] **Step 6: Run compile**

Run:

```bash
mvn test -DskipTests
```

Expected: compile succeeds.

- [ ] **Step 7: Run removal boundary test**

Run:

```bash
mvn test -Dtest=RemovedFeatureBoundaryTest
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add pom.xml src/main/resources/base_sql_v1.0.sql src/test/java/com/admin/base/architecture
git add -u src/main/java/com/admin/base
git commit -m "refactor: remove non-core tool features"
```

## Task 8: MySQL DDL and Seed Drafts

**Files:**
- Create: `docs/database/admin-base-schema-v2.sql`
- Create: `docs/database/admin-base-seed-v2.sql`
- Create: `src/test/java/com/admin/base/database/SchemaDraftTest.java`

**Interfaces:**
- Produces: MySQL DDL draft with retained system tables only and no foreign keys.

- [ ] **Step 1: Create schema draft**

Create `docs/database/admin-base-schema-v2.sql` with:

```sql
SET NAMES utf8mb4;

CREATE TABLE tb_sys_admin (
  admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  nickname VARCHAR(64) NOT NULL COMMENT '昵称',
  user_name VARCHAR(64) NOT NULL COMMENT '登录账号',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
  state TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1禁用',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (admin_id),
  UNIQUE KEY uk_sys_admin_user_name (user_name),
  KEY idx_sys_admin_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统管理员';

CREATE TABLE tb_sys_role (
  role_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
  note VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_sys_role_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色';

CREATE TABLE tb_sys_permissions (
  permission_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父权限ID，0表示根节点',
  level TINYINT NOT NULL COMMENT '层级',
  path VARCHAR(255) NOT NULL COMMENT '前端路由或按钮路径',
  perm VARCHAR(128) NOT NULL COMMENT '权限标识',
  require_auth TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否需要认证：0否，1是',
  state TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  url VARCHAR(255) DEFAULT NULL COMMENT '图标或扩展地址',
  title VARCHAR(64) NOT NULL COMMENT '标题',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (permission_id),
  UNIQUE KEY uk_sys_permissions_perm (perm),
  KEY idx_sys_permissions_parent_id (parent_id),
  KEY idx_sys_permissions_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单和按钮权限';

CREATE TABLE tb_sys_admin_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  admin_id BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_admin_role_admin_role (admin_id, role_id),
  KEY idx_sys_admin_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员角色关系';

CREATE TABLE tb_sys_role_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_permission_role_permission (role_id, permission_id),
  KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系';

CREATE TABLE tb_sys_global_config (
  config_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  config_key VARCHAR(128) NOT NULL COMMENT '配置键',
  config_value VARCHAR(512) NOT NULL COMMENT '配置值',
  note VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_sys_global_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局配置';

CREATE TABLE tb_sys_operation_log (
  operation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  title VARCHAR(64) DEFAULT NULL COMMENT '操作模块',
  business_type TINYINT DEFAULT NULL COMMENT '业务类型',
  method VARCHAR(255) DEFAULT NULL COMMENT 'Java方法',
  request_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
  operation_name VARCHAR(64) DEFAULT NULL COMMENT '操作人员',
  operation_url VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
  operation_ip VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  operation_param JSON DEFAULT NULL COMMENT '请求参数',
  json_result JSON DEFAULT NULL COMMENT '响应结果',
  success TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功：0失败，1成功',
  status_code INT DEFAULT NULL COMMENT '业务或HTTP状态码',
  error_msg TEXT DEFAULT NULL COMMENT '错误消息',
  operation_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  PRIMARY KEY (operation_id),
  KEY idx_sys_operation_log_operation_name (operation_name),
  KEY idx_sys_operation_log_business_type (business_type),
  KEY idx_sys_operation_log_operation_time (operation_time),
  KEY idx_sys_operation_log_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';
```

- [ ] **Step 2: Create seed draft**

Create `docs/database/admin-base-seed-v2.sql` with inserts for one admin, one role, core permissions, role-permission relations, and global config. Use existing BCrypt password hashes from the old script only if acceptable for local seed data:

```sql
INSERT INTO tb_sys_admin (admin_id, nickname, user_name, password, state)
VALUES (1, '管理员', 'admin', '$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi', 0);

INSERT INTO tb_sys_role (role_id, role_name, note)
VALUES (1, 'ROLE_ADMIN', '管理员');

INSERT INTO tb_sys_global_config (config_id, config_key, config_value, note)
VALUES
  (1, 'global_download_path', '/tmp/admin-base/download', '文件下载路径'),
  (2, 'global_upload_path', '/tmp/admin-base/upload', '文件上传路径'),
  (3, 'sys_version', '2.0.0', '系统版本号');
```

- [ ] **Step 3: Add schema draft test**

Create `src/test/java/com/admin/base/database/SchemaDraftTest.java`:

```java
package com.admin.base.database;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDraftTest {

    @Test
    void schemaDraftContainsOnlyRetainedTablesAndNoForeignKeys() throws IOException {
        String ddl = Files.readString(Path.of("docs/database/admin-base-schema-v2.sql")).toLowerCase();

        assertThat(ddl).contains("create table tb_sys_admin");
        assertThat(ddl).contains("create table tb_sys_role");
        assertThat(ddl).contains("create table tb_sys_permissions");
        assertThat(ddl).contains("create table tb_sys_admin_role");
        assertThat(ddl).contains("create table tb_sys_role_permission");
        assertThat(ddl).contains("create table tb_sys_global_config");
        assertThat(ddl).contains("create table tb_sys_operation_log");
        assertThat(ddl).doesNotContain("create table tb_keys");
        assertThat(ddl).doesNotContain("create table tb_records");
        assertThat(ddl).doesNotContain("foreign key");
    }
}
```

- [ ] **Step 4: Run schema draft test**

Run:

```bash
mvn test -Dtest=SchemaDraftTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add docs/database src/test/java/com/admin/base/database/SchemaDraftTest.java
git commit -m "docs: add mysql schema draft for system tables"
```

## Task 9: Logging, Redis Lock, and Sensitive Field Governance

**Files:**
- Modify: `src/main/java/com/admin/base/asp/LogAspect.java`
- Modify: `src/main/java/com/admin/base/asp/RequestLogsAspect.java`
- Modify: `src/main/java/com/admin/base/asp/RepeatInvokeAop.java`
- Modify: `src/main/java/com/admin/base/component/RedisLock.java`
- Create: `src/test/java/com/admin/base/asp/LogAspectSanitizationTest.java`

**Interfaces:**
- Produces: logging policy that does not record password, token, captcha, or file content fields.

- [ ] **Step 1: Add sensitive-field helper**

In `LogAspect`, add:

```java
private static final Set<String> SENSITIVE_KEYS = Set.of(
        "password",
        "oldPassword",
        "newPassword",
        "token",
        "authorization",
        "code",
        "uuid",
        "file"
);

String sanitizeLogPayload(String payload) {
    if (payload == null || payload.isBlank()) {
        return payload;
    }
    String sanitized = payload;
    for (String key : SENSITIVE_KEYS) {
        sanitized = sanitized.replaceAll("(?i)(\"" + key + "\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
    }
    return sanitized;
}
```

- [ ] **Step 2: Apply sanitization before storing operation params**

Where `operationLog.setOperationParam(...)` is called, wrap the value:

```java
operationLog.setOperationParam(sanitizeLogPayload(params));
```

- [ ] **Step 3: Add sanitization test**

Create `src/test/java/com/admin/base/asp/LogAspectSanitizationTest.java`:

```java
package com.admin.base.asp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogAspectSanitizationTest {

    @Test
    void masksSensitiveJsonFields() {
        LogAspect aspect = new LogAspect();

        String result = aspect.sanitizeLogPayload("{\"username\":\"admin\",\"password\":\"secret\",\"token\":\"abc\"}");

        assertThat(result).contains("\"username\":\"admin\"");
        assertThat(result).contains("\"password\":\"***\"");
        assertThat(result).contains("\"token\":\"***\"");
        assertThat(result).doesNotContain("secret");
        assertThat(result).doesNotContain("abc");
    }
}
```

Make `sanitizeLogPayload` package-private so this test can call it.

- [ ] **Step 4: Clarify Redis lock scope**

In `RedisLock`, add class-level Javadoc:

```java
/**
 * Short-lived Redis lock used by repeat-submit protection.
 * This component is not a general-purpose distributed lock abstraction.
 */
```

- [ ] **Step 5: Run logging test**

Run:

```bash
mvn test -Dtest=LogAspectSanitizationTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/admin/base/asp src/main/java/com/admin/base/component/RedisLock.java src/test/java/com/admin/base/asp/LogAspectSanitizationTest.java
git commit -m "refactor: sanitize operation logging"
```

## Task 10: Phase 1 Verification and Documentation

**Files:**
- Modify: `docs/superpowers/specs/2026-07-08-admin-base-modernization-design.md`
- Create: `docs/modernization/phase-1-verification.md`

**Interfaces:**
- Produces: final Phase 1 verification checklist and known follow-up list for Phase 2.

- [ ] **Step 1: Create verification document**

Create `docs/modernization/phase-1-verification.md`:

```markdown
# Phase 1 Verification

## Required Commands

- `mvn test`
- `mvn -DskipTests dependency:tree`
- `rg -n "fastjson2|gson|poi|zxing|javax.annotation-api" pom.xml src/main/java`
- `rg -n "IPage|IService|QueryWrapper" src/main/java/com/admin/base/controller src/main/java/com/admin/base/service/system/*.java`
- `rg -n "tb_keys|tb_records|entity.keys|dto.request.keys|dto.response.keys" src/main/java docs/database/admin-base-schema-v2.sql`

## Expected Results

- Tests pass or each failing integration test has a documented external dependency.
- Heavy tool dependencies are absent from `pom.xml`.
- MyBatis Plus types do not appear in controller return values or service interfaces.
- `tb_keys` and `tb_records` are absent from the v2 schema draft.
- No foreign keys appear in the v2 schema draft.
- JWT mode remains the executable auth mode.
- OAuth2/OIDC remains reserved for a later plan.

## Phase 2 Inputs

- JPA migration plan.
- OAuth2/OIDC provider integration plan.
- Optional Druid removal or retention decision.
- Optional Java 21 decision.
```

- [ ] **Step 2: Run full test suite**

Run:

```bash
mvn test
```

Expected: PASS. If a test requires MySQL or Redis that is not available, record the exact failing test class and the missing service in `docs/modernization/phase-1-verification.md`.

- [ ] **Step 3: Run dependency verification**

Run:

```bash
mvn -DskipTests dependency:tree
```

Expected: command exits 0.

- [ ] **Step 4: Run boundary scans**

Run:

```bash
rg -n "fastjson2|gson|poi|zxing|javax.annotation-api" pom.xml src/main/java || true
rg -n "IPage|IService|QueryWrapper" src/main/java/com/admin/base/controller src/main/java/com/admin/base/service/system/*.java || true
rg -n "foreign key|tb_keys|tb_records" docs/database/admin-base-schema-v2.sql || true
```

Expected:

- First command has no matches.
- Second command has no matches in controller and service interface files.
- Third command has no `foreign key`, `tb_keys`, or `tb_records` matches.

- [ ] **Step 5: Commit**

```bash
git add docs/modernization/phase-1-verification.md docs/superpowers/specs/2026-07-08-admin-base-modernization-design.md
git commit -m "docs: add phase 1 verification checklist"
```

## Self-Review Checklist

- Spec coverage: Phase 1 covers dependency management, safe configuration, compatible response shape, JWT filter governance, service contract decoupling, removal of `tb_keys`/`tb_records`, MySQL DDL draft without foreign keys, and baseline tests.
- Phase 2 boundaries: JPA implementation and real OAuth2/OIDC integration are intentionally excluded and require separate plans.
- Placeholder scan: checked for banned planning markers and found none.
- Type consistency: `JsonResponse<T>`, `PageResult<T>`, `PageQuery`, and `AuthModeProperties` are introduced before later tasks consume them.
- Risk note: Some early web-context tests may expose existing real MySQL/Redis coupling. When that happens, record the exact missing dependency and continue with focused unit tests until configuration tasks remove that coupling.
