# admin_base

`admin_base` 是一个基于 Java 17、Spring Boot 3 和 MySQL/Redis 的后台管理基础项目。它提供了后台系统常见的认证授权、管理员/角色/权限、全局配置、操作日志、验证码、统一响应、统一异常处理、请求日志、重复提交防护和监控指标能力。

项目当前处于现代化 Phase 3 状态：已经完成基础治理、JPA 与 v2 数据库结构迁移、JWT/OAuth2 双认证模式接入。后续新增业务模块时，应优先沿用当前分层、JPA repository、统一响应和权限模型，而不是重新引入旧式模板代码。

## 技术栈

- Java 17
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA / Hibernate
- OAuth2 Resource Server / JWT
- MySQL 8.x
- Redis
- Druid
- Maven
- JUnit 5 / MockMvc / Spring Security Test
- Actuator / Prometheus

## 核心能力

- 认证模式：`admin.auth.mode=jwt` 使用本地登录 + JWT；`admin.auth.mode=oauth2` 使用 OAuth2/OIDC Resource Server。
- 授权模型：管理员绑定角色，角色绑定权限，Controller 使用 `@PreAuthorize` 校验权限字符串。
- 统一响应：Controller 返回 `JsonResponse.success(...)` 或 `JsonResponse.error(...)`。
- 统一异常：业务错误使用 `BusinessException`，由 `GlobalException` 转换为统一 JSON。
- 数据访问：系统核心表使用 Spring Data JPA repository，schema 由 SQL 管理，启动时 `ddl-auto=validate`。
- 审计字段：继承 `CommonDate` / `AuditableEntity` 的实体自动维护 `create_time`、`update_time`。
- 运行监控：Actuator 暴露基础健康与 Prometheus 指标。

## 项目结构

```text
src/main/java/com/admin/base
├── annotation/          # 自定义注解：@Log、@RequestLogs、@RepeatInvoke
├── asp/                 # AOP：操作日志、请求日志、重复提交防护
├── common/              # JsonResponse、PageResult 等通用响应结构
├── component/           # RedisLock、EntityInit、ResponseInit 等组件
├── config/              # Spring、Security、Redis、Druid、JPA 等配置
│   ├── common/          # 通用配置属性
│   └── security/        # JWT/OAuth2/Spring Security 配置与当前用户抽象
├── constant/            # 响应码、状态、缓存类型、日志枚举
├── controller/          # REST Controller
│   ├── common/          # BaseController
│   └── system/          # 系统管理接口
├── dto/                 # request/response DTO
├── entity/              # JPA 实体
│   └── system/          # 系统管理实体
├── exception/           # BusinessException、GlobalException
├── filter/              # MyTokenFilter、XSS wrapper
├── manager/             # 异步日志管理
├── repository/          # Spring Data JPA repository
├── service/             # Service 接口与实现
└── utils/               # JWT、文件、验证码、HTML、UUID 等工具
```

## 本地运行

构建：

```bash
mvn clean package -DskipTests
```

运行测试：

```bash
mvn test
```

运行单个测试类：

```bash
mvn test -Dtest=SeedV2LogicCoverageTest
```

启动应用：

```bash
mvn spring-boot:run
```

指定 profile：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

默认服务配置：

- 端口：`9999`
- Context Path：`/admin-api`
- 默认 profile：`test`

## 配置

核心配置通过 `application.yml` 和 `application-{profile}.yml` 管理。常用环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `test` | Spring profile |
| `SERVER_PORT` | `9999` | 服务端口 |
| `ADMIN_AUTH_MODE` | `jwt` | 认证模式：`jwt` 或 `oauth2` |
| `JWT_SECRET` | 测试密钥 | 本地 JWT HS256 Base64 密钥，生产必须覆盖 |
| `JWT_EXPIRATION_SECONDS` | `7200` | JWT 过期秒数 |
| `ADMIN_OAUTH2_ISSUER_URI` | 空 | OAuth2 issuer，OAuth2 模式必填 |
| `ADMIN_OAUTH2_AUDIENCE` | `admin-api` | JWT audience 校验值 |
| `ADMIN_OAUTH2_USERNAME_CLAIM` | `preferred_username` | OAuth2 用户名 claim |
| `ADMIN_OAUTH2_AUTHORITIES_CLAIM` | `authorities` | OAuth2 权限 claim |

`test` profile 当前连接测试环境 MySQL/Redis。不要在集成测试中私自改成本地服务，除非对应任务明确要求。

## 认证与授权

### JWT 模式

1. `GET /open/captchaImage` 获取验证码。
2. `POST /open/login` 校验账号、密码、验证码，签发 JWT。
3. JWT 写入 Redis，用于请求校验和单点登录控制。
4. 业务请求携带 `Authorization: Bearer <token>`。
5. `MyTokenFilter` 解析 token，加载当前用户和权限。
6. Controller 使用 `@PreAuthorize("hasAuthority('...')")` 做方法级授权。

### OAuth2 模式

设置 `ADMIN_AUTH_MODE=oauth2` 后，应用作为 OAuth2/OIDC Resource Server 接收外部 IdP 签发的 Bearer token。配置方式见 [OAuth2 Provider Setup](docs/modernization/oauth2-provider-setup.md)。

OAuth2 权限会经过 `OAuth2AuthorityMapper` 归一化后进入 Spring Security。业务代码获取当前用户时应使用 `CurrentUserProvider`，不要直接假设 principal 一定是本地 `UserDetailsImpl`。

## 数据库

v2 schema 与种子数据位于：

```text
docs/database/admin-base-schema-v2.sql
docs/database/admin-base-schema-v2-migration.sql
docs/database/admin-base-seed-v2.sql
```

数据库约定：

- 使用 MySQL 8.x。
- 结构由 SQL 脚本管理，不由 Hibernate 自动建表。
- `spring.jpa.hibernate.ddl-auto=validate` 用于启动时校验实体与表结构。
- DDL 不创建外键，通过唯一索引、普通索引、Service 校验和测试保证一致性。
- 新表应包含必要主键、唯一约束、查询索引，以及 `create_time`、`update_time` 审计列。
- 种子数据需要显式写入审计列，避免 MySQL strict mode 下插入失败。

## 后续新增模块如何扩展

新增模块建议按“数据库 -> 实体 -> Repository -> Service -> DTO -> Controller -> 权限 -> 测试 -> 文档”的顺序推进。

### 1. 设计数据结构

- 在 `docs/database/` 新增或更新 schema/migration SQL。
- 表名建议使用 `tb_<domain>_<module>` 或与现有系统表风格一致。
- 不添加数据库外键；需要唯一性时添加唯一索引。
- 对高频查询字段添加普通索引。
- 如实体需要审计时间，使用 `create_time`、`update_time`，并让实体继承 `AuditableEntity`。

### 2. 添加实体和 Repository

- 实体放在 `src/main/java/com/admin/base/entity/<domain>/`。
- Repository 放在 `src/main/java/com/admin/base/repository/<domain>/`，继承 `JpaRepository`。
- 用 `@Table`、`@Column` 显式固定表名、列名、长度和非空约束。
- 避免新增 MyBatis Mapper；当前主路径是 Spring Data JPA。

### 3. 添加 Service

- Service 接口放在 `service/<domain>/`，实现放在 `service/<domain>/impl/`。
- 业务校验放在 Service 层，不要塞进 Controller。
- 业务错误抛 `BusinessException`，让全局异常处理统一响应。
- 分页结果使用 `PageResult` 或项目现有分页封装，保持接口形状一致。

### 4. 添加 DTO

- 请求 DTO 放在 `dto/request/<domain>/`。
- 响应 DTO 放在 `dto/response/<domain>/`。
- 使用 Bean Validation 注解声明必填、长度、范围等规则。
- 不要直接把实体作为复杂业务接口的请求体暴露给前端。

### 5. 添加 Controller

- Controller 放在 `controller/<domain>/` 或现有 `controller/system/`。
- 返回 `JsonResponse`，不要返回裸对象或自行拼 JSON。
- 继承 `BaseController` 以复用分页表格和当前用户能力。
- 需要当前用户时优先使用 `getUserName()` 或注入 `CurrentUserProvider`。

### 6. 添加权限

- 菜单级权限继续兼容现有命名，如 `sys:adminList`。
- 按钮级权限使用冒号分隔，如 `sys:admin:add`、`sys:admin:edit`、`sys:admin:delete`。
- Controller 的 `@PreAuthorize` 必须和 seed 数据中的 `perm` 保持一致。
- 新模块需要同步更新角色权限种子数据，并补覆盖测试。

### 7. 添加测试

- 纯业务逻辑优先写普通 JUnit + Mockito。
- Controller/安全边界使用 `MockMvc` 和 `spring-security-test`。
- 数据库结构和 seed 变更需要更新 `SchemaSeedConsistencyTest` 或新增同类测试。
- 涉及远端测试环境时使用 `test` profile，不要在测试里启动本地 MySQL/Redis。
- 对 seed 驱动的逻辑建议使用事务内 fixture，测试结束回滚。

### 8. 更新文档

- 模块加入 README 的能力说明或扩展说明。
- Agent 开发规则更新到 `AGENTS.md`。
- 架构性变更补充到 `docs/modernization/` 或 `docs/superpowers/`。

## 测试策略

常用命令：

```bash
# 全量测试
mvn test

# 安全配置与 OAuth2 相关测试
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest

# v2 seed 逻辑覆盖测试
mvn test -Dtest=SeedV2LogicCoverageTest
```

测试重点：

- schema 与 seed 一致性。
- JWT 与 OAuth2 两种认证模式。
- `@PreAuthorize` 权限字符串与 seed 数据一致。
- 统一响应和异常形状稳定。
- 新模块的 Service 校验、分页、权限和核心接口闭环。

## 现代化文档

- [第一阶段：基础治理](docs/modernization/admin-base-phase-1-readable-plan.md)
- [第二阶段：JPA 与数据库迁移](docs/modernization/admin-base-phase-2-readable-plan.md)
- [第三阶段：OAuth2/OIDC 接入](docs/modernization/admin-base-phase-3-readable-plan.md)
- [Phase 3 验证记录](docs/modernization/phase-3-verification.md)
- [OAuth2 Provider Setup](docs/modernization/oauth2-provider-setup.md)

详细执行计划位于：

```text
docs/superpowers/plans/
```

总体设计文档位于：

```text
docs/superpowers/specs/2026-07-08-admin-base-modernization-design.md
```
