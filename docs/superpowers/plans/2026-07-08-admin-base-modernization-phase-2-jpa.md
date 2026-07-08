# Admin Base Modernization Phase 2 JPA Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the internal MyBatis Plus persistence implementation with Spring Data JPA after Phase 1 has hidden MyBatis Plus from controller and service contracts.

**Architecture:** Phase 2 keeps public API response shape and service-facing behavior stable while replacing mapper implementations module by module. JPA entities use conservative relationships: explicit `AdminRole` and `RolePermission` association entities remain, bidirectional relationships and broad cascade rules are avoided. Database schema follows the Phase 1 MySQL v2 DDL draft with indexes only and no foreign keys.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Data JPA, Hibernate, MySQL, Redis, Spring Security, Jackson, JUnit 5, MockMvc, Testcontainers MySQL.

## Global Constraints

- Continue using MySQL.
- Do not create database foreign keys; use primary keys, unique indexes, normal indexes, service checks, and tests.
- Use `spring-boot-starter-parent` for dependency and plugin management.
- Keep Java at 17 unless a separate Java 21 decision is made.
- Keep existing response exterior `{code,msg,data}`.
- Keep existing APIs basically compatible; obvious errors may be fixed with compatibility notes.
- Default auth mode is local JWT; OAuth2/OIDC implementation is handled by the Phase 3 plan.
- Do not reintroduce `tb_keys`, `tb_records`, keyword-recognition code, Office/PDF code, QR code code, or steganography/watermark code.
- Remove MyBatis Plus, mapper interfaces, mapper XML, and MyBatis configuration only after each affected module has passing repository and service tests.
- Use conservative JPA entity modeling: no broad `CascadeType.REMOVE`, no entity exposure from controllers, no reliance on Open Session in View.
- Do not include unrelated existing worktree changes in commits.

---

## File Structure Map

- `pom.xml`: remove MyBatis Plus dependencies, add `spring-boot-starter-data-jpa`.
- `src/main/resources/application*.yml`: remove `mybatis-plus` config, add JPA config.
- `src/main/java/com/admin/base/entity/system/*.java`: convert MyBatis Plus annotations to Jakarta Persistence annotations.
- `src/main/java/com/admin/base/entity/CommonDate.java`: replace with JPA mapped superclass or remove after entity conversion.
- `src/main/java/com/admin/base/repository/system/*.java`: Spring Data JPA repositories.
- `src/main/java/com/admin/base/service/system/impl/*.java`: switch from `ServiceImpl<Mapper, Entity>` to repository constructor injection.
- `src/main/java/com/admin/base/mapper/**`: delete after module migration.
- `src/main/resources/mapper/*.xml`: delete after module migration.
- `src/test/java/com/admin/base/repository/system/*.java`: repository tests.
- `src/test/java/com/admin/base/service/system/*.java`: service tests.
- `src/test/java/com/admin/base/architecture/PersistenceBoundaryTest.java`: verifies no MyBatis Plus remains after migration.

## Task 1: Add JPA Test Infrastructure

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application-test.yml`
- Create: `src/test/java/com/admin/base/repository/RepositoryTestSupport.java`
- Create: `src/test/java/com/admin/base/architecture/PersistenceBoundaryTest.java`

**Interfaces:**
- Produces: repository test foundation and MyBatis-removal boundary assertions.

- [ ] **Step 1: Add JPA dependency while MyBatis still exists**

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

Keep MyBatis Plus dependencies until Task 8.

- [ ] **Step 2: Add JPA test properties**

In `src/main/resources/application-test.yml`, add:

```yaml
spring:
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
```

- [ ] **Step 3: Add repository boundary test**

Create `src/test/java/com/admin/base/architecture/PersistenceBoundaryTest.java`:

```java
package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceBoundaryTest {

    @Test
    void mybatisPlusDoesNotAppearInControllersOrServiceInterfaces() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/com/admin/base"))) {
            String combined = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/controller/")
                            || path.toString().matches(".*service/system/I[^/]+\\.java"))
                    .map(this::read)
                    .reduce("", String::concat);

            assertThat(combined).doesNotContain("com.baomidou.mybatisplus");
            assertThat(combined).doesNotContain("IPage<");
            assertThat(combined).doesNotContain("IService<");
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
```

- [ ] **Step 4: Run boundary test**

Run:

```bash
mvn test -Dtest=PersistenceBoundaryTest
```

Expected: PASS if Phase 1 service-contract decoupling is complete.

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/resources/application-test.yml src/test/java/com/admin/base/architecture/PersistenceBoundaryTest.java
git commit -m "test: add jpa migration boundary checks"
```

## Task 2: Convert Core Entities to JPA

**Files:**
- Modify: `src/main/java/com/admin/base/entity/system/Admin.java`
- Modify: `src/main/java/com/admin/base/entity/system/Role.java`
- Modify: `src/main/java/com/admin/base/entity/system/Permissions.java`
- Modify: `src/main/java/com/admin/base/entity/system/AdminRole.java`
- Modify: `src/main/java/com/admin/base/entity/system/RolePermission.java`
- Modify: `src/main/java/com/admin/base/entity/system/GlobalConfig.java`
- Modify: `src/main/java/com/admin/base/entity/system/OperationLog.java`
- Create: `src/main/java/com/admin/base/entity/AuditableEntity.java`

**Interfaces:**
- Produces: JPA entities mapped to v2 MySQL table names and columns.

- [ ] **Step 1: Add audit mapped superclass**

Create `src/main/java/com/admin/base/entity/AuditableEntity.java`:

```java
package com.admin.base.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: Enable JPA auditing**

Add to the main application class `src/main/java/com/admin/base/BaseApplication.java`:

```java
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
```

- [ ] **Step 3: Convert `Admin`**

Replace MyBatis Plus annotations with:

```java
@Entity
@Table(name = "tb_sys_admin", indexes = {
        @Index(name = "idx_sys_admin_state", columnList = "state")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_admin_user_name", columnNames = "user_name")
})
public class Admin extends AuditableEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "nickname", nullable = false, length = 64)
    private String nickname;

    @Column(name = "user_name", nullable = false, length = 64)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "state", nullable = false)
    private Integer state;
}
```

Remove `passwordShow`.

- [ ] **Step 4: Convert remaining entities**

For each retained system entity, use:

```java
@Entity
@Table(name = "table_name")
```

Use `Long` for identifiers:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "role_id")
private Long roleId;
```

Keep `AdminRole` and `RolePermission` as explicit association entities with scalar ids:

```java
@Column(name = "admin_id", nullable = false)
private Long adminId;

@Column(name = "role_id", nullable = false)
private Long roleId;
```

Do not add `@ManyToMany` in this phase.

- [ ] **Step 5: Update DTO and service id types deliberately**

Do not mass-change request DTO ids yet. In service implementations, convert request `Integer` ids to `Long` using:

```java
private Long toLongId(Integer id) {
    return id == null ? null : id.longValue();
}
```

Controller compatibility remains unchanged.

- [ ] **Step 6: Run compile**

Run:

```bash
mvn test -DskipTests
```

Expected: compile errors identify every remaining MyBatis Plus annotation or id type mismatch. Fix only entity and implementation compile errors in this task.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/admin/base/entity src/main/java/com/admin/base/BaseApplication.java
git commit -m "refactor: convert system entities to jpa mappings"
```

## Task 3: Add Spring Data Repositories

**Files:**
- Create: `src/main/java/com/admin/base/repository/system/AdminRepository.java`
- Create: `src/main/java/com/admin/base/repository/system/RoleRepository.java`
- Create: `src/main/java/com/admin/base/repository/system/PermissionsRepository.java`
- Create: `src/main/java/com/admin/base/repository/system/AdminRoleRepository.java`
- Create: `src/main/java/com/admin/base/repository/system/RolePermissionRepository.java`
- Create: `src/main/java/com/admin/base/repository/system/GlobalConfigRepository.java`
- Create: `src/main/java/com/admin/base/repository/system/OperationLogRepository.java`
- Create: `src/test/java/com/admin/base/repository/system/GlobalConfigRepositoryTest.java`

**Interfaces:**
- Produces: repository methods consumed by service implementations.

- [ ] **Step 1: Create simple repositories**

Example `GlobalConfigRepository`:

```java
package com.admin.base.repository.system;

import com.admin.base.entity.system.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long> {
    Optional<GlobalConfig> findByConfigKey(String configKey);
    boolean existsByConfigKey(String configKey);
}
```

- [ ] **Step 2: Create admin repository**

```java
package com.admin.base.repository.system;

import com.admin.base.entity.system.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserName(String userName);
    boolean existsByUserName(String userName);
    Page<Admin> findByUserNameContaining(String userName, Pageable pageable);
}
```

- [ ] **Step 3: Create relation repositories**

`AdminRoleRepository`:

```java
package com.admin.base.repository.system;

import com.admin.base.entity.system.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    boolean existsByAdminIdAndRoleId(Long adminId, Long roleId);
    List<AdminRole> findByAdminId(Long adminId);
    void deleteByAdminIdAndRoleId(Long adminId, Long roleId);
    void deleteByAdminId(Long adminId);
}
```

`RolePermissionRepository`:

```java
package com.admin.base.repository.system;

import com.admin.base.entity.system.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
    void deleteByPermissionId(Long permissionId);
}
```

- [ ] **Step 4: Create repository test**

Create `GlobalConfigRepositoryTest`:

```java
package com.admin.base.repository.system;

import com.admin.base.entity.system.GlobalConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GlobalConfigRepositoryTest {

    @Autowired
    private GlobalConfigRepository repository;

    @Test
    void findsConfigByKey() {
        GlobalConfig config = new GlobalConfig();
        config.setConfigKey("sys_version");
        config.setConfigValue("2.0.0");
        repository.save(config);

        assertThat(repository.findByConfigKey("sys_version")).isPresent();
    }
}
```

- [ ] **Step 5: Run repository test**

Run:

```bash
mvn test -Dtest=GlobalConfigRepositoryTest
```

Expected: PASS with test database configuration. If `@DataJpaTest` lacks a test database, switch this class to Testcontainers MySQL before changing repository code.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/admin/base/repository src/test/java/com/admin/base/repository
git commit -m "feat: add jpa repositories for system tables"
```

## Task 4: Migrate Global Config Module

**Files:**
- Modify: `src/main/java/com/admin/base/service/system/impl/GlobalConfigServiceImpl.java`
- Test: `src/test/java/com/admin/base/service/system/GlobalConfigServiceTest.java`

**Interfaces:**
- Consumes: `GlobalConfigRepository`, `PageResult<GlobalConfig>`.
- Produces: first module running on JPA repository.

- [ ] **Step 1: Write service test**

Create `GlobalConfigServiceTest` with mocked repository:

```java
package com.admin.base.service.system;

import com.admin.base.entity.system.GlobalConfig;
import com.admin.base.repository.system.GlobalConfigRepository;
import com.admin.base.service.system.impl.GlobalConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalConfigServiceTest {

    @Test
    void selectByKeyReturnsConfig() {
        GlobalConfigRepository repository = Mockito.mock(GlobalConfigRepository.class);
        GlobalConfigServiceImpl service = new GlobalConfigServiceImpl(repository);
        GlobalConfig config = new GlobalConfig();
        config.setConfigKey("sys_version");
        config.setConfigValue("2.0.0");
        when(repository.findByConfigKey("sys_version")).thenReturn(Optional.of(config));

        GlobalConfig result = service.selectByKey("sys_version");

        assertThat(result.getConfigValue()).isEqualTo("2.0.0");
    }
}
```

- [ ] **Step 2: Refactor implementation constructor**

Change `GlobalConfigServiceImpl` from extending `ServiceImpl` to:

```java
@Service
@RequiredArgsConstructor
public class GlobalConfigServiceImpl implements IGlobalConfigService {

    private final GlobalConfigRepository globalConfigRepository;
}
```

- [ ] **Step 3: Replace mapper operations**

Use repository calls:

```java
public GlobalConfig selectByKey(String key) {
    return globalConfigRepository.findByConfigKey(key).orElse(null);
}
```

For pagination:

```java
Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
Page<GlobalConfig> result = globalConfigRepository.findAll(pageable);
return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
```

- [ ] **Step 4: Run service test**

Run:

```bash
mvn test -Dtest=GlobalConfigServiceTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/admin/base/service/system/impl/GlobalConfigServiceImpl.java src/test/java/com/admin/base/service/system/GlobalConfigServiceTest.java
git commit -m "refactor: migrate global config service to jpa"
```

## Task 5: Migrate Operation Log Module

**Files:**
- Modify: `src/main/java/com/admin/base/service/system/impl/OperationLogServiceImpl.java`
- Test: `src/test/java/com/admin/base/service/system/OperationLogServiceTest.java`

**Interfaces:**
- Consumes: `OperationLogRepository`, `PageResult<OperationLog>`.
- Produces: operation log service with JPA pagination and sorting.

- [ ] **Step 1: Add repository query method**

In `OperationLogRepository`:

```java
Page<OperationLog> findAll(Pageable pageable);
```

- [ ] **Step 2: Refactor service class**

Use:

```java
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements IOperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Override
    public void insertOperationLog(OperationLog operationLog) {
        operationLogRepository.save(operationLog);
    }
}
```

- [ ] **Step 3: Implement pagination**

```java
public PageResult<OperationLog> listPage(OperationLogListParam param) {
    Pageable pageable = PageRequest.of(param.getPage() - 1, param.getSize(), Sort.by(Sort.Direction.DESC, "operationTime"));
    Page<OperationLog> page = operationLogRepository.findAll(pageable);
    return new PageResult<>(page.getContent(), page.getTotalElements(), param.getPage(), param.getSize());
}
```

- [ ] **Step 4: Run service tests**

Run:

```bash
mvn test -Dtest=OperationLogServiceTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/admin/base/repository/system/OperationLogRepository.java src/main/java/com/admin/base/service/system/impl/OperationLogServiceImpl.java src/test/java/com/admin/base/service/system/OperationLogServiceTest.java
git commit -m "refactor: migrate operation log service to jpa"
```

## Task 6: Migrate Role and Permission Modules

**Files:**
- Modify: `src/main/java/com/admin/base/service/system/impl/RoleServiceImpl.java`
- Modify: `src/main/java/com/admin/base/service/system/impl/PermissionsServiceImpl.java`
- Modify: `src/main/java/com/admin/base/service/system/impl/RolePermissionServiceImpl.java`
- Test: `src/test/java/com/admin/base/service/system/RolePermissionServiceTest.java`

**Interfaces:**
- Consumes: `RoleRepository`, `PermissionsRepository`, `RolePermissionRepository`.
- Produces: role/permission services without mapper calls.

- [ ] **Step 1: Add repository methods**

`RoleRepository`:

```java
boolean existsByRoleName(String roleName);
Optional<Role> findByRoleName(String roleName);
```

`PermissionsRepository`:

```java
List<Permissions> findByState(Integer state);
List<Permissions> findByPermissionIdIn(List<Long> ids);
List<Permissions> findByParentId(Long parentId);
```

- [ ] **Step 2: Refactor role service**

Replace mapper calls with:

```java
roleRepository.save(role);
roleRepository.deleteById(roleId.longValue());
roleRepository.existsByRoleName(roleName);
```

- [ ] **Step 3: Refactor permission tree reads**

Load enabled permissions:

```java
List<Permissions> permissions = permissionsRepository.findByState(1);
```

Build tree in service memory using `parentId`.

- [ ] **Step 4: Refactor role-permission updates**

Use explicit relation replacement:

```java
rolePermissionRepository.deleteByRoleId(roleId.longValue());
for (Integer permissionId : permissionIds) {
    RolePermission relation = new RolePermission();
    relation.setRoleId(roleId.longValue());
    relation.setPermissionId(permissionId.longValue());
    rolePermissionRepository.save(relation);
}
```

- [ ] **Step 5: Run service tests**

Run:

```bash
mvn test -Dtest=RolePermissionServiceTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/admin/base/repository/system src/main/java/com/admin/base/service/system/impl/RoleServiceImpl.java src/main/java/com/admin/base/service/system/impl/PermissionsServiceImpl.java src/main/java/com/admin/base/service/system/impl/RolePermissionServiceImpl.java src/test/java/com/admin/base/service/system/RolePermissionServiceTest.java
git commit -m "refactor: migrate role and permission services to jpa"
```

## Task 7: Migrate Admin and Authentication Persistence

**Files:**
- Modify: `src/main/java/com/admin/base/service/system/impl/AdminServiceImpl.java`
- Modify: `src/main/java/com/admin/base/service/system/impl/AdminRoleServiceImpl.java`
- Modify: `src/main/java/com/admin/base/config/security/UserDetailsServiceImpl.java`
- Test: `src/test/java/com/admin/base/service/system/AdminServiceTest.java`

**Interfaces:**
- Consumes: `AdminRepository`, `AdminRoleRepository`, `RoleRepository`, `RolePermissionRepository`, `PermissionsRepository`.
- Produces: admin and auth lookup paths without mapper calls.

- [ ] **Step 1: Refactor admin lookup**

Use:

```java
public Admin selectByUserName(String username) {
    return adminRepository.findByUserName(username)
            .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "不存在该管理员！"));
}
```

- [ ] **Step 2: Refactor add admin**

Use `adminRepository.existsByUserName(username)`, `adminRepository.save(admin)`, then create `AdminRole` rows with `adminRoleRepository.save(...)`.

- [ ] **Step 3: Refactor auth role loading**

Use:

```java
List<AdminRole> relations = adminRoleRepository.findByAdminId(adminId);
List<Long> roleIds = relations.stream().map(AdminRole::getRoleId).toList();
List<Role> roles = roleRepository.findAllById(roleIds);
```

- [ ] **Step 4: Refactor permission loading**

Use role-permission relation rows, then `permissionsRepository.findAllById(permissionIds)`.

- [ ] **Step 5: Run admin service tests**

Run:

```bash
mvn test -Dtest=AdminServiceTest
```

Expected: PASS.

- [ ] **Step 6: Run login/auth tests**

Run:

```bash
mvn test -Dtest=OpenEndpointTest,SecurityBoundaryTest
```

Expected: PASS or documented missing Redis dependency if integration environment is absent.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/admin/base/service/system/impl/AdminServiceImpl.java src/main/java/com/admin/base/service/system/impl/AdminRoleServiceImpl.java src/main/java/com/admin/base/config/security/UserDetailsServiceImpl.java src/test/java/com/admin/base/service/system/AdminServiceTest.java
git commit -m "refactor: migrate admin persistence to jpa"
```

## Task 8: Remove MyBatis Plus and Mapper XML

**Files:**
- Modify: `pom.xml`
- Delete: `src/main/java/com/admin/base/mapper/`
- Delete: `src/main/resources/mapper/`
- Delete: `src/main/java/com/admin/base/config/MybatisInterceptor.java`
- Modify: `src/main/resources/application*.yml`
- Test: `src/test/java/com/admin/base/architecture/PersistenceBoundaryTest.java`

**Interfaces:**
- Produces: project without MyBatis Plus runtime dependencies.

- [ ] **Step 1: Remove MyBatis dependencies**

Delete from `pom.xml`:

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

- [ ] **Step 2: Remove mapper source and XML**

Run:

```bash
git rm -r src/main/java/com/admin/base/mapper
git rm -r src/main/resources/mapper
git rm src/main/java/com/admin/base/config/MybatisInterceptor.java
```

- [ ] **Step 3: Remove MyBatis configuration**

Delete `mybatis-plus:` sections from all `src/main/resources/application*.yml` files.

- [ ] **Step 4: Strengthen boundary test**

Add to `PersistenceBoundaryTest`:

```java
@Test
void mybatisPlusIsRemovedFromMainSources() throws IOException {
    try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
        String combined = files
                .filter(path -> path.toString().endsWith(".java"))
                .map(this::read)
                .reduce("", String::concat);

        assertThat(combined).doesNotContain("com.baomidou.mybatisplus");
    }
}
```

- [ ] **Step 5: Run compile and boundary tests**

Run:

```bash
mvn test -Dtest=PersistenceBoundaryTest
mvn test -DskipTests
```

Expected: both commands pass.

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/resources/application*.yml src/test/java/com/admin/base/architecture/PersistenceBoundaryTest.java
git add -u src/main/java src/main/resources
git commit -m "refactor: remove mybatis plus persistence layer"
```

## Task 9: Apply V2 Schema and Seed Verification

**Files:**
- Modify: `docs/database/admin-base-schema-v2.sql`
- Modify: `docs/database/admin-base-seed-v2.sql`
- Create: `src/test/java/com/admin/base/database/SchemaSeedConsistencyTest.java`

**Interfaces:**
- Produces: schema and seed scripts matching JPA entity table and column names.

- [ ] **Step 1: Add schema consistency test**

Create `SchemaSeedConsistencyTest`:

```java
package com.admin.base.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaSeedConsistencyTest {

    @Test
    void schemaUsesIndexesWithoutForeignKeys() throws Exception {
        String schema = Files.readString(Path.of("docs/database/admin-base-schema-v2.sql")).toLowerCase();

        assertThat(schema).contains("create table tb_sys_admin");
        assertThat(schema).contains("unique key uk_sys_admin_user_name");
        assertThat(schema).doesNotContain("foreign key");
        assertThat(schema).doesNotContain("tb_keys");
        assertThat(schema).doesNotContain("tb_records");
    }

    @Test
    void seedOnlyTargetsRetainedTables() throws Exception {
        String seed = Files.readString(Path.of("docs/database/admin-base-seed-v2.sql")).toLowerCase();

        assertThat(seed).contains("insert into tb_sys_admin");
        assertThat(seed).contains("insert into tb_sys_role");
        assertThat(seed).doesNotContain("tb_keys");
        assertThat(seed).doesNotContain("tb_records");
    }
}
```

- [ ] **Step 2: Run schema tests**

Run:

```bash
mvn test -Dtest=SchemaSeedConsistencyTest,SchemaDraftTest
```

Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add docs/database src/test/java/com/admin/base/database/SchemaSeedConsistencyTest.java
git commit -m "test: verify v2 schema and seed scripts"
```

## Task 10: Final Phase 2 Verification

**Files:**
- Create: `docs/modernization/phase-2-verification.md`

**Interfaces:**
- Produces: evidence checklist for completed JPA migration.

- [ ] **Step 1: Create verification document**

Create `docs/modernization/phase-2-verification.md`:

```markdown
# Phase 2 Verification

## Required Commands

- `mvn test`
- `mvn -DskipTests dependency:tree`
- `rg -n "com.baomidou.mybatisplus|mybatis-plus|QueryWrapper|BaseMapper|IPage|IService" pom.xml src/main/java src/main/resources`
- `rg -n "foreign key|tb_keys|tb_records" docs/database/admin-base-schema-v2.sql docs/database/admin-base-seed-v2.sql`

## Expected Results

- Tests pass.
- MyBatis Plus dependencies and imports are absent.
- Mapper interfaces and XML files are absent.
- JPA repositories back system modules.
- V2 schema has no foreign keys.
- V2 schema and seed scripts exclude `tb_keys` and `tb_records`.
```

- [ ] **Step 2: Run full verification**

Run:

```bash
mvn test
mvn -DskipTests dependency:tree
rg -n "com.baomidou.mybatisplus|mybatis-plus|QueryWrapper|BaseMapper|IPage|IService" pom.xml src/main/java src/main/resources || true
rg -n "foreign key|tb_keys|tb_records" docs/database/admin-base-schema-v2.sql docs/database/admin-base-seed-v2.sql || true
```

Expected: first two commands exit 0. Last two scans have no matches.

- [ ] **Step 3: Commit**

```bash
git add docs/modernization/phase-2-verification.md
git commit -m "docs: add phase 2 verification checklist"
```

## Self-Review Checklist

- Spec coverage: Covers JPA migration, MyBatis Plus removal, mapper XML removal, v2 schema verification, no foreign keys, and retained system modules.
- Phase 1 dependency: Assumes Phase 1 has already hidden MyBatis Plus types from controller and service interfaces.
- Phase 3 boundary: Does not implement OAuth2/OIDC.
- Type consistency: repository ids are `Long`; controller request DTO compatibility remains unchanged by converting `Integer` ids inside service implementations.
- Planning marker scan: checked for banned markers and found none.

