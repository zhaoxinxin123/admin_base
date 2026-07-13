# admin_base

`admin_base` 是一个基于 Java 17、Spring Boot 3.5、Spring Data JPA、Spring Security、MySQL 和 Redis 的后台管理基础项目。
## 技术栈

- Java 17
- Spring Boot 3.5.16
- Spring Security / OAuth2 Resource Server
- Spring Data JPA / Hibernate
- MySQL 8.x
- Redis
- Druid
- Maven
- JUnit 5 / MockMvc / Spring Security Test
- Actuator / Prometheus

## 核心能力

- 本地 JWT 登录：验证码、账号密码登录、Redis token 校验、方法级权限控制。
- OAuth2 Resource Server：可切换到外部 IdP 签发的 Bearer token。
- 系统管理：管理员、角色、权限、全局配置、操作日志。
- 统一 Web 形状：`JsonResponse`、`PageResult`、全局异常处理、Bean Validation。
- 持久化：使用 Spring Data JPA repository，数据库结构由 SQL 管理，Hibernate 只做 `ddl-auto=validate`。
- 基础设施：Redis 缓存、重复提交防护、操作日志 AOP、请求日志、文件上传下载、Actuator 指标。

## 项目结构

```text
src/main/java/com/admin/base
├── auth/                         # 登录、验证码、认证 DTO 和开放接口
│   ├── dto/
│   └── web/
├── infrastructure/               # 横切基础设施
│   ├── aop/                      # @Log、@RequestLogs、@RepeatInvoke 及切面
│   ├── async/                    # 异步任务管理
│   ├── bootstrap/                # 启动初始化组件
│   ├── cache/                    # Redis 缓存与锁
│   ├── config/                   # Spring、Security、JPA、Redis、Druid 配置
│   ├── security/                 # JWT/OAuth2、当前用户抽象、权限映射
│   └── web/                      # 通用上传下载接口、BaseController
├── shared/                       # 通用 API、常量、异常、领域基类、工具类
│   ├── api/
│   ├── constant/
│   ├── domain/
│   ├── exception/
│   └── util/
├── system/                       # 系统管理业务域
│   ├── admin/
│   ├── config/
│   ├── log/
│   ├── permission/
│   └── role/
│       ├── application/          # Service 接口与实现
│       ├── domain/               # JPA Entity
│       ├── dto/                  # request/response DTO
│       ├── persistence/          # Spring Data JPA repository
│       └── web/                  # REST Controller
└── user/                         # Spring Security 用户加载与用户 DTO
```

每个业务模块优先采用 `web -> application -> persistence/domain` 的调用方向：Controller 只做入参校验和响应包装，业务规则放在 Service，数据库访问放在 Repository。

## 快速开始

### 1. 准备环境

- JDK 17
- Maven 3.9+
- MySQL 8.x
- Redis 6+

默认服务配置：

- 端口：`9999`
- Context Path：`/admin-api`
- 默认 profile：`test`
- 默认认证模式：`jwt`

`test` profile 连接共享测试环境 `192.168.3.3` 上的 MySQL 和 Redis。开发本地环境建议使用 `dev` profile，并通过环境变量配置本机数据库和 Redis。

### 2. 创建数据库并导入数据

v2 数据库脚本位于：

```text
docs/database/admin-base-schema-v2.sql
docs/database/admin-base-schema-v2-migration.sql
docs/database/admin-base-seed-v2.sql
```

全新数据库初始化：

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS admin_base CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -uroot -p admin_base < docs/database/admin-base-schema-v2.sql
mysql -uroot -p admin_base < docs/database/admin-base-seed-v2.sql
```

已有旧库迁移到 v2：

```bash
mysql -uroot -p admin_base < docs/database/admin-base-schema-v2-migration.sql
mysql -uroot -p admin_base < docs/database/admin-base-seed-v2.sql
```

导入后会得到：

- 默认管理员：`admin`
- 默认密码：`123456`
- 默认角色：`ROLE_ADMIN`
- 核心权限：系统管理、管理员、角色、权限、全局配置、操作日志
- 全局配置：上传路径、下载路径、系统版本号

脚本约定：

- 不创建数据库外键。
- 通过唯一索引、普通索引、Service 校验和测试保证一致性。
- 表结构由 SQL 维护，应用启动时只做实体校验。
- 新增或修改 seed 权限时，同步更新角色权限关联和测试覆盖。

### 3. 配置本地运行参数

`dev` profile 默认连接本机 MySQL/Redis，也可以用环境变量覆盖：

```bash
export SPRING_PROFILES_ACTIVE=dev
export DEV_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/admin_base?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false'
export DEV_DATASOURCE_USERNAME=root
export DEV_DATASOURCE_PASSWORD=''
export DEV_REDIS_HOST=127.0.0.1
export DEV_REDIS_PORT=6379
export JWT_SECRET='YnJvLXN0cmluZy1hZG1pbi1iYXNlLWp3dC1zZWNyZXQ='
```

生产环境必须显式配置数据库、Redis、上传下载路径和安全密钥：

```bash
export SPRING_PROFILES_ACTIVE=prod
export PROD_DATASOURCE_URL='jdbc:mysql://mysql.example.com:3306/admin_base?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false'
export PROD_DATASOURCE_USERNAME='admin_base'
export PROD_DATASOURCE_PASSWORD='change-me'
export PROD_REDIS_HOST='redis.example.com'
export PROD_UPLOAD_PATH='/data/admin-base/upload'
export PROD_DOWNLOAD_PATH='/data/admin-base/download'
export PROD_LOCAL_STORE='/data/admin-base/local'
export JWT_SECRET='base64-encoded-32-byte-or-longer-secret'
```

### 4. 构建、测试和启动

```bash
# 构建，不跑测试
mvn clean package -DskipTests

# 全量测试
mvn test

# 运行重点测试类
mvn test -Dtest=SeedV2LogicCoverageTest
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest

# 启动，默认 profile 为 test
mvn spring-boot:run

# 启动 dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

启动后访问基础地址：

```text
http://localhost:9999/admin-api
```

## 配置说明

常用环境变量：

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
| `DEV_DATASOURCE_URL` | 本机 `admin_base` | dev 数据库连接 |
| `DEV_REDIS_HOST` | `127.0.0.1` | dev Redis 地址 |
| `PROD_DATASOURCE_URL` | 无 | prod 数据库连接，必填 |
| `PROD_REDIS_HOST` | 无 | prod Redis 地址，必填 |
| `PROD_UPLOAD_PATH` | 无 | prod 上传目录，必填 |
| `PROD_DOWNLOAD_PATH` | 无 | prod 下载目录，必填 |
| `PROD_LOCAL_STORE` | 无 | prod 本地存储目录，必填 |

## 认证与授权

### JWT 模式

默认 `ADMIN_AUTH_MODE=jwt`。

1. `GET /open/captchaImage` 获取验证码 `uuid` 和图片。
2. `POST /open/login` 提交账号、密码、验证码和 `uuid`。
3. 应用签发本地 JWT，并把有效 token 写入 Redis。
4. 后续请求携带 `Authorization: Bearer <token>`。
5. `MyTokenFilter` 校验 token，加载当前用户、角色和权限。
6. 受保护接口使用 `@PreAuthorize("hasAuthority('...')")` 做方法级授权。

### OAuth2 模式

设置 `ADMIN_AUTH_MODE=oauth2` 后，应用作为 OAuth2/OIDC Resource Server 接收外部 IdP 签发的 Bearer token。

```bash
export ADMIN_AUTH_MODE=oauth2
export ADMIN_OAUTH2_ISSUER_URI='https://issuer.example.com/'
export ADMIN_OAUTH2_AUDIENCE='admin-api'
```

OAuth2 权限会经过 `OAuth2AuthorityMapper` 归一化后进入 Spring Security。业务代码需要当前用户时，使用 `CurrentUserProvider` 或 `BaseController#getUserName()`，不要假设 principal 一定是本地 `UserDetailsImpl`。

更多配置见 [OAuth2 Provider Setup](docs/modernization/oauth2-provider-setup.md)。

## 接口测试

下面示例以本地 `dev` profile 和 JWT 模式为例。

### 1. 获取验证码

```bash
curl -s http://localhost:9999/admin-api/open/captchaImage | jq
```

响应包含 `uuid` 和 base64 图片。开发环境当前响应中也包含验证码临时值，接口联调时可直接使用；生产环境不应依赖该字段。

### 2. 登录获取 token

```bash
curl -s -X POST http://localhost:9999/admin-api/open/login \
  -H 'Content-Type: application/json' \
  -d '{
    "userName": "admin",
    "password": "123456",
    "code": "验证码",
    "uuid": "上一步返回的 uuid"
  }' | jq
```

保存返回的 token：

```bash
export ADMIN_TOKEN='登录响应中的 token'
```

### 3. 调用受保护接口

```bash
curl -s -X POST http://localhost:9999/admin-api/admin/getMenu \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq
```

常用系统接口：

| 模块 | 接口 | 说明 |
| --- | --- | --- |
| 开放接口 | `GET /open/captchaImage` | 获取验证码 |
| 开放接口 | `POST /open/login` | 登录 |
| 管理员 | `POST /admin/add` | 新增管理员 |
| 管理员 | `POST /admin/delete` | 删除管理员 |
| 管理员 | `POST /admin/getMenu` | 当前用户菜单 |
| 管理员角色 | `POST /admin_role/list` | 管理员列表 |
| 管理员角色 | `POST /admin_role/resetPassword` | 重置密码 |
| 管理员角色 | `POST /admin_role/updateState` | 更新管理员状态 |
| 管理员角色 | `POST /admin_role/updateAdminOfRole` | 更新管理员角色 |
| 角色 | `POST /role/all` | 全部角色 |
| 角色 | `POST /role/add` | 新增角色 |
| 角色 | `POST /role/delete` | 删除角色 |
| 角色 | `POST /role/update` | 更新角色 |
| 角色权限 | `POST /role_permission/manageList` | 角色权限树 |
| 权限 | `POST /permissions/list` | 权限树 |
| 权限 | `POST /permissions/add` | 新增权限 |
| 权限 | `POST /permissions/update` | 更新权限 |
| 权限 | `POST /permissions/delete` | 删除权限 |
| 全局配置 | `POST /sys_global_config/list` | 配置列表 |
| 全局配置 | `POST /sys_global_config/add` | 新增配置 |
| 全局配置 | `POST /sys_global_config/update` | 更新配置 |
| 全局配置 | `POST /sys_global_config/deleteBatch` | 批量删除配置 |
| 操作日志 | `POST /sys_operation_log/list` | 操作日志列表 |
| 操作日志 | `POST /sys_operation_log/deleteBatch` | 批量删除操作日志 |
| 文件 | `POST /common/upload` | 上传文件 |
| 文件 | `GET /common/download` | 下载文件 |

如果接口返回无权限，检查三处是否一致：

- Controller 上的 `@PreAuthorize` 权限字符串。
- `docs/database/admin-base-seed-v2.sql` 中 `tb_sys_permissions.perm`。
- `tb_sys_role_permission` 中当前角色是否拥有该权限。

## 数据库与 seed 开发

新增或修改数据库对象时，按以下顺序处理：

1. 修改 `docs/database/admin-base-schema-v2.sql`，保证全量建库脚本正确。
2. 修改 `docs/database/admin-base-schema-v2-migration.sql`，保证旧库可迁移。
3. 修改 `docs/database/admin-base-seed-v2.sql`，保证默认数据、权限和角色权限关系完整。
4. 实体类补齐 `@Table`、`@Column`、长度、空值约束和索引对应关系。
5. 运行 schema/seed 测试：

```bash
mvn test -Dtest=SchemaDraftTest,SchemaSeedConsistencyTest,SeedV2LogicCoverageTest
```

项目约定：

- 不新增数据库外键。
- 不让 Hibernate 自动建表或更新表。
- 有 `create_time`、`update_time` 的实体继承 `AuditableEntity`。
- seed SQL 必须写入审计列，避免 MySQL strict mode 下插入失败。
- 权限 seed 变更时，补 seed 驱动逻辑覆盖测试。

## 新增业务模块

新增模块建议按“数据库 -> Entity -> Repository -> DTO -> Service -> Controller -> 权限 -> 测试 -> 文档”的顺序推进。

假设新增 `notice` 模块，推荐目录如下：

```text
src/main/java/com/admin/base/system/notice
├── application/
│   ├── INoticeService.java
│   └── NoticeServiceImpl.java
├── domain/
│   └── Notice.java
├── dto/
│   ├── AddNoticeParam.java
│   ├── NoticeResponse.java
│   └── UpdateNoticeParam.java
├── persistence/
│   └── NoticeRepository.java
└── web/
    └── NoticeController.java
```

### 1. 数据库

- 在 `docs/database/` 更新 schema、migration 和 seed。
- 表名使用清晰的业务前缀，例如 `tb_sys_notice`。
- 添加主键、唯一索引、查询索引和审计列。
- 不添加数据库外键；跨表一致性放到 Service 校验和测试中。

### 2. Entity

- 放在 `system/<module>/domain/`。
- 显式声明 `@Table`、`@Column`、长度、非空约束。
- 有审计字段时继承 `AuditableEntity`。
- 字段类型和 SQL 保持一致，避免 `ddl-auto=validate` 失败。

### 3. Repository

- 放在 `system/<module>/persistence/`。
- 继承 `JpaRepository<Entity, Long>`。
- 优先使用派生查询或 JPQL，不新增 MyBatis Mapper/XML。

### 4. DTO

- 放在 `system/<module>/dto/`。
- 请求 DTO 使用 Bean Validation，例如 `@NotBlank`、`@Size`、`@NotNull`。
- 响应 DTO 不直接暴露复杂实体关系。

### 5. Service

- 接口和实现放在 `system/<module>/application/`。
- 业务校验、唯一性校验、状态流转和跨表检查都放在 Service。
- 业务失败抛 `BusinessException`，使用合适的 `ResponseCode`。
- 分页结果沿用 `PageResult` 和 `BaseController#getDataTable(...)` 的形状。

### 6. Controller

- 放在 `system/<module>/web/`。
- Controller 保持薄：校验 DTO、调用 Service、返回 `JsonResponse`。
- 每个受保护接口添加明确的 `@PreAuthorize`。
- 需要当前用户时使用 `BaseController#getUserName()` 或 `CurrentUserProvider`。

### 7. 权限

- 菜单级权限保留兼容风格，例如 `sys:noticeList`。
- 按钮级权限使用冒号分隔，例如 `sys:notice:add`、`sys:notice:edit`、`sys:notice:delete`。
- seed 中新增权限后，同步把默认管理员角色映射到这些权限。
- Controller 的权限字符串必须和 seed 中的 `perm` 一致。

### 8. 测试

- Service 纯逻辑使用 JUnit/Mockito。
- Controller 和安全边界使用 MockMvc 与 `spring-security-test`。
- schema/seed 变更运行 `SchemaDraftTest`、`SchemaSeedConsistencyTest` 和 `SeedV2LogicCoverageTest`。
- 涉及共享测试环境时使用 `test` profile，不要在测试中私自切换本地 MySQL/Redis 或 Testcontainers。

## 测试策略

常用测试：

```bash
# 全量测试
mvn test

# 架构边界
mvn test -Dtest=PackageStructureBoundaryTest,PersistenceBoundaryTest,RemovedFeatureBoundaryTest

# 统一响应、异常、工具
mvn test -Dtest=JsonResponseTest,PageResultTest,BusinessExceptionTest,JwtTokenUtilTest

# JWT 安全边界
mvn test -Dtest=SecurityConfigTest,SecurityBoundaryTest,OpenEndpointTest

# OAuth2
mvn test -Dtest=AuthModePropertiesTest,AuthModeSecurityConfigTest,OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,OAuth2ResourceServerTest,SecurityContextCurrentUserProviderTest

# 数据库脚本和 seed
mvn test -Dtest=SchemaDraftTest,SchemaSeedConsistencyTest,SeedV2LogicCoverageTest
```
## 代码约定

- 新代码使用 Spring Data JPA repository，不新增 MyBatis Mapper/XML。
- Controller 返回 `JsonResponse.success(...)` 或 `JsonResponse.error(...)`。
- 业务校验放 Service，业务异常用 `BusinessException`。
- 受保护接口必须有明确的 `@PreAuthorize`。
- 不记录密码、JWT、验证码答案、OAuth2 token 等敏感值。
- 改动保持聚焦，避免把按钮权限整体迁移等大任务混入无关需求。
- 生产默认值保持安全；测试和开发默认值必须在文档中说明。
