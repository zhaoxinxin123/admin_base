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
- 权限边界：25 个受保护接口由可执行权限矩阵校验，文件上传、下载和下载后删除使用独立权限。
- 业务完整性：权限树禁止形成环并支持递归删除，角色名称执行前缀、长度和唯一性校验。
- 统一 Web 形状：`JsonResponse`、`PageResult`、全局异常处理、Bean Validation。
- 持久化：使用 Spring Data JPA repository，数据库结构由 SQL 管理，Hibernate 只做 `ddl-auto=validate`。
- 基础设施：Redis 缓存、重复提交防护、操作日志 AOP、请求日志、文件上传下载、Actuator 指标。

## 项目结构

仓库中的主要目录和文档如下：

```text
admin_base/
├── docs/
│   ├── api/                      # 可导入 Apifox 的 OpenAPI 文档
│   ├── database/                 # v2 全量 schema 与 seed
│   ├── superpowers/              # 安全测试改造的设计与实施记录
│   └── testing/                  # dev 环境标准测试流程
├── src/
│   ├── main/
│   │   ├── java/                 # 应用源码
│   │   └── resources/            # application*.yml
│   └── test/java/                # 单元、边界和集成测试
├── AGENTS.md                     # 本仓库的开发代理规则
├── README.md
└── pom.xml
```

Java 主代码的实际包结构如下：

```text
src/main/java/com/admin/base
├── BaseApplication.java          # Spring Boot 启动类
├── auth/                         # 登录、验证码、认证 DTO 和开放接口
│   ├── controller/               # CaptchaController、LoginController
│   └── dto/                      # CaptchaResponse、LoginParam、LoginResponse
├── infrastructure/               # 横切基础设施
│   ├── aop/                      # 日志、请求记录、重复调用防护及 annotation/
│   ├── async/                    # 异步任务管理及 factory/
│   ├── cache/                    # Redis 缓存与锁
│   ├── config/                   # Bean、CORS、时间序列化、Redis 和 SysConfig
│   ├── controller/               # BaseController、CommonController
│   ├── filter/                   # MyTokenFilter、XssHttpServletRequestWrapper
│   └── security/                 # JWT/OAuth2、用户加载、当前用户抽象、权限映射
├── shared/                       # 通用 API、常量、异常、实体基类、工具类
│   ├── api/                      # JsonResponse、分页模型及 dto/
│   ├── constant/
│   ├── entity/                   # AuditableEntity、CommonDate JPA 基类
│   ├── exception/
│   ├── factory/                  # EntityFactory、ResponseFactory 对象装配工厂
│   └── util/
├── system/                       # 系统管理业务域
│   ├── admin/                    # 管理员与管理员角色关系
│   ├── config/                   # 全局配置
│   ├── log/                      # 操作日志
│   ├── permission/               # 权限树
│   └── role/                     # 角色与角色权限关系
└── user/                         # 用户查询 DTO 与服务
    ├── dto/
    └── service/
        └── impl/
```

`system` 下的五个模块都各自拥有完整分层，不是只有 `role` 模块分层：

```text
system/<module>/
├── controller/                   # REST Controller
├── dto/                          # 请求和响应 DTO
├── entity/                       # JPA Entity
├── repository/                   # Spring Data JPA Repository
└── service/                      # Service 接口
    └── impl/                     # Service 实现
```

业务模块采用 `controller -> service -> repository/entity` 的调用方向：Controller 只做入参校验和响应包装，业务规则放在 Service，数据库访问放在 Repository。Spring Security 的用户加载实现位于 `infrastructure/security/UserDetailsServiceImpl`；`user` 包只负责用户查询 DTO 与服务。

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

默认启动会读取 `application-test.yml`，其默认数据源是共享测试服务器上的 `mark-test`。手工开发建议显式使用 `dev` profile；`application-dev.yml` 默认连接本机的 `admin_base` 和 Redis。

仓库中的远程集成测试同样声明为 `dev` profile，但通过 `DevRemoteIntegrationTest` 覆盖为 `192.168.3.3` 上的 MySQL/Redis 和 `admin_base_it`。破坏性全接口测试只允许使用名称以 `_it` 结尾的数据库。不要为了执行这些测试启动本地 MySQL 或 Redis；环境变化时使用 `DEV_*` 变量覆盖。

### 2. 创建数据库并导入数据

v2 数据库脚本位于：

```text
docs/database/admin-base-schema-v2.sql
docs/database/admin-base-seed-v2.sql
```

全新数据库初始化：

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS admin_base CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -uroot -p admin_base < docs/database/admin-base-schema-v2.sql
mysql -uroot -p admin_base < docs/database/admin-base-seed-v2.sql
```

集成测试使用独立的可清空数据库 `admin_base_it`。首次运行远程白盒测试前，在测试服务器上初始化：

```bash
mysql -h192.168.3.3 -uroot -p -e "CREATE DATABASE IF NOT EXISTS admin_base_it CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -h192.168.3.3 -uroot -p admin_base_it < docs/database/admin-base-schema-v2.sql
mysql -h192.168.3.3 -uroot -p admin_base_it < docs/database/admin-base-seed-v2.sql
```

`FullApiAuthorizationIntegrationTest` 会重置核心表，因此只允许连接名称以 `_it` 结尾的数据库。数据库保护器会拒绝 `admin_base` 等非测试库。

当前仓库没有维护增量 migration 脚本；旧库升级时请先备份，再根据 v2 全量 schema 做受控迁移。

导入后会得到：

- 默认管理员：`admin`
- 默认密码：`123456`
- 默认角色：`ROLE_ADMIN`
- 核心权限：系统管理、管理员、角色、权限、全局配置、操作日志、文件上传/下载/删除
- 全局配置：上传路径、下载路径、系统版本号

脚本约定：

- 不创建数据库外键。
- 通过唯一索引、普通索引、Service 校验和测试保证一致性。
- 表结构由 SQL 维护，应用启动时只做实体校验。
- 新增或修改 seed 权限时，同步更新角色权限关联和测试覆盖。

### 3. 配置运行参数

手工使用 `dev` profile 时默认连接本机 MySQL/Redis，也可以用环境变量覆盖：

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
mvn test -Dtest=FullApiAuthorizationIntegrationTest
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest

# 启动默认 test profile；会读取 application-test.yml
mvn spring-boot:run

# 推荐的手工开发启动方式
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

规范测试流程见 [dev 环境测试流程](docs/testing/dev-test-process.md)。接口文档可直接导入 Apifox：`docs/api/admin-base-openapi.apifox.yaml`。

启动后访问基础地址：

```text
http://localhost:9999/admin-api
```

## 配置说明

各 profile 的数据源边界：

| 使用场景 | 生效配置 | 默认 MySQL | 默认 Redis |
| --- | --- | --- | --- |
| 默认启动 | `application-test.yml` | `192.168.3.3:3306/mark-test` | `192.168.3.3:6379` |
| 手工 `dev` 启动 | `application-dev.yml` | `127.0.0.1:3306/admin_base` | `127.0.0.1:6379` |
| 远程集成测试 | `dev` + `DevRemoteIntegrationTest` | `192.168.3.3:3306/admin_base_it` | `192.168.3.3:6379` |
| 生产启动 | `application-prod.yml` | 必须通过环境变量提供 | 必须通过环境变量提供 |

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
| `TEST_DATASOURCE_URL` | 远程 `mark-test` | test 数据库连接 |
| `TEST_DATASOURCE_USERNAME` | `root` | test 数据库用户 |
| `TEST_DATASOURCE_PASSWORD` | 配置文件测试值 | test 数据库密码，建议通过环境变量覆盖 |
| `TEST_REDIS_HOST` | `192.168.3.3` | test Redis 地址 |
| `TEST_REDIS_PORT` | `6379` | test Redis 端口 |
| `TEST_REDIS_PASSWORD` | 配置文件测试值 | test Redis 密码，建议通过环境变量覆盖 |
| `DEV_DATASOURCE_URL` | 本机 `admin_base` | dev 数据库连接 |
| `DEV_DATASOURCE_USERNAME` | `root` | dev 数据库用户 |
| `DEV_DATASOURCE_PASSWORD` | 空 | dev 数据库密码 |
| `DEV_REDIS_HOST` | `127.0.0.1` | dev Redis 地址 |
| `DEV_REDIS_PORT` | `6379` | dev Redis 端口 |
| `DEV_REDIS_PASSWORD` | 空 | dev Redis 密码 |
| `PROD_DATASOURCE_URL` | 无 | prod 数据库连接，必填 |
| `PROD_DATASOURCE_USERNAME` | 无 | prod 数据库用户，必填 |
| `PROD_DATASOURCE_PASSWORD` | 无 | prod 数据库密码，必填 |
| `PROD_REDIS_HOST` | 无 | prod Redis 地址，必填 |
| `PROD_REDIS_PORT` | `6379` | prod Redis 端口 |
| `PROD_REDIS_PASSWORD` | 空 | prod Redis 密码 |
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

使用 `dev` 或 `prod` profile 并设置 `ADMIN_AUTH_MODE=oauth2` 后，应用作为 OAuth2/OIDC Resource Server 接收外部 IdP 签发的 Bearer token。`application-test.yml` 将认证模式固定为 `jwt`，不要用默认 test profile 验证 OAuth2 运行模式。

```bash
export ADMIN_AUTH_MODE=oauth2
export ADMIN_OAUTH2_ISSUER_URI='https://issuer.example.com/'
export ADMIN_OAUTH2_AUDIENCE='admin-api'
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

IdP 必须提供与 issuer 匹配的 OIDC discovery/JWKS，并在 JWT 中提供配置的 audience、用户名 claim 和权限 claim。OAuth2 权限会经过 `OAuth2AuthorityMapper` 归一化后进入 Spring Security。业务代码需要当前用户时，使用 `CurrentUserProvider` 或 `BaseController#getUserName()`，不要假设 principal 一定是本地 `UserDetailsImpl`。

`OAuth2ResourceServerTest` 会启动测试用 OIDC discovery/JWKS 服务，使用真实 RSA 签名 JWT 验证 issuer、audience、权限映射和 OAuth2 操作日志用户名，不使用模拟 `JwtDecoder`。

## 接口测试

下面示例以本地 `dev` profile 和 JWT 模式为例。

完整接口文档位于 [admin-base-openapi.apifox.yaml](docs/api/admin-base-openapi.apifox.yaml)，可通过 Apifox 的 OpenAPI/Swagger 导入功能导入。导入后配置环境变量：

```text
baseUrl = http://localhost:9999/admin-api
token = 登录接口返回的 data.token
```

### 1. 获取验证码

```bash
curl -s http://localhost:9999/admin-api/open/captchaImage | jq
```

当前实现的响应包含 `uuid`、base64 图片 `img` 和测试字段 `tmpVar`，并会把验证码写入应用日志；该行为没有按 profile 隔离。`tmpVar` 可用于当前接口联调，但生产部署前必须移除响应字段和明文日志，客户端不得依赖它。

### 2. 登录获取 token

```bash
curl -s -X POST http://localhost:9999/admin-api/open/login \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin",
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

当前全部 REST 接口：

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
| 文件 | `GET /common/download/resource2` | 本地资源下载 |

文件接口权限：

| 接口 | 所需权限 |
| --- | --- |
| `POST /common/upload` | `sys:file:upload` |
| `GET /common/download` | `sys:file:download`；`delete=true` 时还需要 `sys:file:delete` |
| `GET /common/download/resource2` | `sys:file:download` |

如果接口返回无权限，检查三处是否一致：

- Controller 上的 `@PreAuthorize` 权限字符串。
- `docs/database/admin-base-seed-v2.sql` 中 `tb_sys_permissions.perm`。
- `tb_sys_role_permission` 中当前角色是否拥有该权限。

## 数据库与 seed 开发

新增或修改数据库对象时，按以下顺序处理：

1. 修改 `docs/database/admin-base-schema-v2.sql`，保证全量建库脚本正确。
2. 修改 `docs/database/admin-base-seed-v2.sql`，保证默认数据、权限和角色权限关系完整。
3. 如任务需要增量升级，新增并记录 migration 脚本，同时更新 `SchemaSeedConsistencyTest`、README 和 `AGENTS.md`。
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
- 权限节点更新时不能把自身或后代设置为父节点；删除节点会递归删除整棵子树及角色权限关联。
- 角色名必须以 `ROLE_` 开头、总长度不超过 10，并在新增和更新时保持唯一。

## 新增业务模块

新增模块建议按“数据库 -> Entity -> Repository -> DTO -> Service -> Controller -> 权限 -> 测试 -> 文档”的顺序推进。

假设新增 `notice` 模块，推荐目录如下：

```text
src/main/java/com/admin/base/system/notice
├── controller/
│   └── NoticeController.java
├── dto/
│   ├── AddNoticeParam.java
│   ├── NoticeResponse.java
│   └── UpdateNoticeParam.java
├── entity/
│   └── Notice.java
├── repository/
│   └── NoticeRepository.java
└── service/
    ├── INoticeService.java
    └── impl/
        └── NoticeServiceImpl.java
```

### 1. 数据库

- 在 `docs/database/` 更新 schema 和 seed；如需要旧库增量升级，再新增 migration。
- 表名使用清晰的业务前缀，例如 `tb_sys_notice`。
- 添加主键、唯一索引、查询索引和审计列。
- 不添加数据库外键；跨表一致性放到 Service 校验和测试中。

### 2. Entity

- 放在 `system/<module>/entity/`。
- 显式声明 `@Table`、`@Column`、长度、非空约束。
- 有审计字段时继承 `AuditableEntity`。
- 字段类型和 SQL 保持一致，避免 `ddl-auto=validate` 失败。

### 3. Repository

- 放在 `system/<module>/repository/`。
- 继承 `JpaRepository<Entity, Long>`。
- 优先使用派生查询或 JPQL，不新增 MyBatis Mapper/XML。

### 4. DTO

- 放在 `system/<module>/dto/`。
- 请求 DTO 使用 Bean Validation，例如 `@NotBlank`、`@Size`、`@NotNull`。
- 响应 DTO 不直接暴露复杂实体关系。

### 5. Service

- 接口和实现放在 `system/<module>/service/`（实现类在 `service/impl/`）。
- 业务校验、唯一性校验、状态流转和跨表检查都放在 Service。
- 业务失败抛 `BusinessException`，使用合适的 `ResponseCode`。
- 分页结果沿用 `PageResult` 和 `BaseController#getDataTable(...)` 的形状。

### 6. Controller

- 放在 `system/<module>/controller/`。
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
- 涉及共享测试环境时使用 `dev` profile，默认远端地址为 `192.168.3.3`，破坏性测试仅可连接 `admin_base_it`，不要在测试中私自切换本地 MySQL/Redis 或 Testcontainers。

## 测试策略

测试分为三类：不依赖外部服务的单元/边界测试、连接远程 MySQL/Redis 的 `dev` 集成测试，以及使用本地测试 JWKS HTTP 服务但不依赖外部 IdP 的 OAuth2 资源服务器测试。执行全量测试时不启动本地 MySQL 或 Redis。

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

# JWT 全接口、管理员/部分权限用户、操作日志
mvn test -Dtest=FullApiAuthorizationIntegrationTest

# OAuth2
mvn test -Dtest=AuthModePropertiesTest,AuthModeSecurityConfigTest,OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,OAuth2ResourceServerTest,SecurityContextCurrentUserProviderTest

# 数据库脚本和 seed
mvn test -Dtest=SchemaDraftTest,SchemaSeedConsistencyTest,SeedV2LogicCoverageTest

# 精确权限、文件、权限树、角色及测试库保护
mvn test -Dtest=ApiPermissionMatrixTest,CommonControllerTest,PermissionsServiceImplTest,RoleServiceImplTest,DestructiveTestDatabaseGuardTest
```

全接口白盒测试覆盖管理员全接口操作、仅拥有 `sys:adminList` 的受限用户、公开接口、错误路径、文件内容/响应头/删除行为和操作日志完整性。`ApiPermissionMatrixTest` 固化全部 25 个受保护接口的权限表达式。详细流程和通过标准见 [dev 环境测试流程](docs/testing/dev-test-process.md)。

## 代码约定

- 新代码使用 Spring Data JPA repository，不新增 MyBatis Mapper/XML。
- Controller 返回 `JsonResponse.success(...)` 或 `JsonResponse.error(...)`。
- 业务校验放 Service，业务异常用 `BusinessException`。
- 受保护接口必须有明确的 `@PreAuthorize`。
- 不记录密码、JWT、验证码答案、OAuth2 token 等敏感值。
- 改动保持聚焦，避免把按钮权限整体迁移等大任务混入无关需求。
- 生产默认值保持安全；测试和开发默认值必须在文档中说明。
