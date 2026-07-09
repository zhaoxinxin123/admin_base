# admin_base 现代化改造设计

日期：2026-07-08

## 1. 背景与目标

`admin_base` 是一个 Java 17、Spring Boot、Spring Security、MyBatis Plus、MySQL、Redis 后台管理模板项目。项目已经从 Spring Boot 2 升级到 Spring Boot 3，但仍保留了不少历史包袱：依赖冗余、配置中含敏感信息、MyBatis Plus 类型向 controller/service 外露、数据库字段设计不一致、公共组件职责混杂、测试基线薄弱，以及部分专用业务工具混入核心模板。

本次改造目标不是重写项目，而是把它整理成一个可以长期维护的 Spring Boot 后台模板。改造采用“治理先行，迁移后置”的两阶段路线。

第一阶段是基础治理：依赖、配置、安全、统一响应、异常、日志、测试、数据库字段审查、MyBatis Plus 解耦。第一阶段不直接替换 JPA，不大规模改包结构，不重写全部 API。

第二阶段是迁移重塑：在第一阶段稳定边界后，将持久层从 MyBatis Plus 迁移到 Spring Data JPA，整理实体、Repository、Service、DDL、包结构，并按独立计划接入 OAuth2/OIDC。

明确不做事项：

- 不替换 MySQL。
- 不在第一阶段强行接入 OAuth2 登录中心。
- 不在第一阶段重写所有 API。
- 不创建数据库外键约束。
- 不保留关键词识别、Office/PDF、二维码、隐写/水印等专用能力。
- 不以 Spring Boot 最新版为目标，而以社会采用度优先。

## 2. 已确认决策

- 总路线：方案一，治理先行，迁移后置。
- API 策略：基本兼容现有前端/API，明显错误进入兼容修复清单。
- 数据策略：保留基础种子数据，清理业务和历史日志数据。
- Spring Boot 版本原则：社会采用度优先，目标暂定 Spring Boot 3.5.x 最新补丁线。
- 依赖管理：采用 `spring-boot-starter-parent` 管理依赖和 Maven 插件版本。
- Java 版本：继续使用 Java 17，除非后续单独决定升级 Java 21。
- 数据库：继续使用 MySQL。
- DDL 约束：只做主键、唯一索引、普通索引、默认值、注释，不做外键。
- 移除表：`tb_keys`、`tb_records`。
- 认证授权：默认 Spring Security + 本地 JWT，预留 OAuth2/OIDC 启动时切换结构。
- Token filter：当前在 Security 链路中使用，不移除，改为治理和重构。
- 持久层：第一阶段先解耦 MyBatis Plus 类型，第二阶段迁移 Spring Data JPA。
- JPA 风格：保守落地，少建复杂双向关系，优先显式关联实体和 service 查询组装。
- 统一返回：保持 `{code,msg,data}` 外观兼容，内部泛型化和规范化。
- 工具能力：保留后台模板通用能力，移除专用重型工具。
- 测试：先补最小安全网，再边改边补完整模块测试。

## 3. 阶段规划

### 第一阶段：基础治理

目标：

- 让项目依赖更清晰。
- 让配置更安全。
- 保持接口基本兼容。
- 补充最小测试基线。
- 规范统一返回、异常、日志、认证过滤器。
- 从 controller/service 对外契约中移除 MyBatis Plus 类型。
- 完成 MySQL 字段审查和新版 DDL 草案。

范围：

- `pom.xml` 治理。
- profile 和敏感配置治理。
- `JsonResponse<T>`、`PageResult<T>`、异常体系治理。
- JWT filter 和 Spring Security 配置治理。
- MyBatis Plus 对外类型解耦。
- 删除 `tb_keys`、`tb_records` 及相关业务代码的计划和清单。
- 输出核心系统表新版 DDL 草案。
- 补齐基线测试。

第一阶段验收标准：

- `mvn test` 可通过。
- 核心接口响应结构保持 `{code,msg,data}`。
- controller 返回值和 service 接口不暴露 `IPage`、`IService`、`QueryWrapper` 等 MyBatis Plus 类型。
- `pom.xml` 已切到 `spring-boot-starter-parent`。
- 敏感配置不再硬编码提交。
- `tb_keys`、`tb_records` 及专用工具能力进入移除范围。
- MySQL 新版 DDL 草案完成，且不包含外键。
- JWT 模式可用，OAuth2/OIDC 预留结构清楚。
- 基础种子数据策略明确。

### 第二阶段：JPA 与结构迁移

目标：

- 将持久层实现从 MyBatis Plus 替换为 Spring Data JPA。
- 删除 Mapper、Mapper XML、MyBatis Plus 依赖和配置。
- 按新版 MySQL DDL 初始化核心系统表。
- 逐步把包结构向业务模块收敛。
- OAuth2/OIDC 根据独立计划接入具体认证中心。

范围：

- `spring-boot-starter-data-jpa`。
- Jakarta Persistence 实体。
- Spring Data JPA Repository。
- Service 层事务边界。
- DDL 和种子数据执行。
- 模块化包结构演进。

第二阶段验收标准：

- MyBatis Plus、Mapper XML、MyBatis 配置移除。
- Spring Data JPA Repository 替换完成。
- 核心表按新版 DDL 初始化。
- 管理员、角色、权限、配置、日志功能通过测试。
- 包结构逐步收敛到业务模块。
- OAuth2/OIDC 可按独立子计划接入具体认证中心。

## 4. 子计划

### 基础治理计划

- 清理 `pom.xml`，切换到 `spring-boot-starter-parent`。
- 外置敏感配置，修正 profile 策略。
- 清理重复实体、旧包、无用配置。
- 统一代码规范、命名和注入风格。
- 建立基础测试环境。

### 依赖治理计划

- 目标版本暂定 Spring Boot 3.5.x 最新补丁线。
- 删除当前手动导入的 `spring-boot-dependencies` BOM。
- Spring Boot 相关依赖和 Maven 插件版本交给 parent 管理。
- 删除重复 MyBatis Plus starter。
- 第一阶段保留 MyBatis Plus 运行能力，开始接口解耦。
- 删除 Office/PDF、二维码、隐写/水印、关键词识别相关依赖。
- JSON 统一到 Jackson，移除 Gson/Fastjson 的业务依赖。
- 升级 `jjwt 0.9.1`，或评估使用 Spring Security OAuth2 JOSE 的 JWT 能力。
- 移除 `javax.annotation-api`，统一 Jakarta 注解。
- MySQL 驱动、Redis、Actuator、Security、Validation、AOP 保留为核心能力。
- Druid 是否保留单独评估：如果模板偏标准化，优先 HikariCP；如果仍需要监控页，则保留并规范配置。

版本依据：

- OpenLogic 2026 Java 生态报告显示，Spring Boot 用户中 3.5 占 52.05%。
- Spring 官方当前最新是 Spring Boot 4.1.0，但最新不等于采用最多。
- Spring Boot 3.5 的社区支持已在 2026-06-30 结束，此风险需要明确记录。

参考资料：

- OpenLogic Java Ecosystem Trends: https://www.openlogic.com/blog/java-ecosystem-trends
- Spring Boot 官方页: https://spring.io/projects/spring-boot
- OpenLogic Spring LTS: https://www.openlogic.com/solutions/spring/long-term-support
- Spring Boot 3.5 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes

### 数据库字段审查与 DDL 计划

核心保留表：

- `tb_sys_admin`
- `tb_sys_role`
- `tb_sys_permissions`
- `tb_sys_admin_role`
- `tb_sys_role_permission`
- `tb_sys_global_config`
- `tb_sys_operation_log`

明确移除表：

- `tb_keys`
- `tb_records`

DDL 规则：

- 不创建外键。
- 保留主键、唯一索引、普通索引。
- 主键统一使用 `bigint unsigned` 或 `bigint`，不再混用 `int(11)`、`int(20)`、`bigint(20)`。
- 状态字段统一使用 `tinyint`。
- 布尔语义字段使用 `tinyint(1)`。
- 时间字段统一为 `datetime(3)`，字段名保留 `create_time`、`update_time`。
- 是否增加 `delete_time` 取决于是否启用软删除。
- 删除 `password_show` 等明文或展示密码字段。
- 操作日志大字段使用 `text` 或 `json`。
- 角色、权限、用户名、配置 key 等业务唯一性通过唯一索引表达。
- 关联表不建外键，但保留组合唯一索引和查询索引。
- 字段命名使用小写下划线，Java 使用驼峰映射。
- 表注释、字段注释、索引命名统一。

已识别问题：

- `tb_sys_admin.state` SQL 是 `varchar(255)`，Java 是 `Integer`，需要改为 `tinyint`。
- `tb_sys_admin.password_show` 需要移除。
- `tb_sys_permissions.parent_id` 与主键类型不一致，需要统一。
- `tb_sys_operation_log.status` 注释是“0 正常 1 异常”，但数据里存过 `200`，需要重新定义为 `success` 或 `status_code`。
- `tb_sys_operation_log.operation_param` 容量不足。
- `tb_sys_admin_role`、`tb_sys_role_permission` 保留唯一索引，但不建外键。

种子数据策略：

- 保留或重建管理员、角色、权限菜单、基础配置。
- 清理业务数据和历史操作日志。
- `tb_keys`、`tb_records` 不迁移、不生成种子数据。

### 认证授权计划

默认模式：

```yaml
admin:
  auth:
    mode: jwt
```

JWT 模式完整落地：

- 保留 `/open/login`。
- 保留验证码登录流程。
- 保留 Spring Security + 自签 JWT。
- 保留 Redis 单点登录和踢下线能力。
- `MyTokenFilter` 当前通过 `SecurityConfig.addFilterBefore(...)` 加入过滤链，不移除。
- 将 `MyTokenFilter` 治理为职责清晰的 JWT 认证过滤器。
- 升级 JWT 依赖或评估 Spring Security JOSE JWT 能力。
- 清理 filter 中直接写响应、直接 new Gson、CORS header、Bearer 截取、异常码等问题。
- 认证失败、token 过期、被挤下线统一走兼容 `{code,msg,data}` 响应。

OAuth2/OIDC 预留模式：

```yaml
admin:
  auth:
    mode: oauth2
```

第一阶段不强制接 Keycloak、Casdoor、Spring Authorization Server 或企业统一登录，只预留结构：

- `AuthModeProperties`
- `CurrentUserProvider`
- `AuthorityMapper`
- `JwtSecurityConfig`
- `OAuth2ResourceServerSecurityConfig`
- `TokenAuthenticationStrategy` 或类似边界

切换方式是启动时配置切换，不做运行时热切换。

权限模型：

- 继续使用角色和权限字符串。
- `@PreAuthorize("hasAuthority('permission-code')")` 这类权限校验方式第一阶段基本保留。
- 明显错误的权限标识进入兼容修复清单，例如 `sys:adminList:diable`。
- 权限加载逻辑从认证方式中抽离。
- JWT 模式从本地用户、角色、权限加载。
- OAuth2 模式未来从 claims 或用户信息映射。

安全配置治理：

- 使用 Spring Security 6 推荐写法。
- `@EnableGlobalMethodSecurity` 替换为 `@EnableMethodSecurity`。
- 明确公开端点：`/open/**`、必要的 health/prometheus、错误页。
- Druid 如果保留，访问策略单独设计；如果移除 Druid，则删除 `/druid/**` 白名单。
- 默认无状态 session。
- CORS 从 token filter 中移出，统一由 CORS 配置管理。
- Redis token key、过期时间、刷新策略统一封装。

### 公共组件治理计划

统一返回：

- 保持 `{code,msg,data}` 外观。
- `JsonResponse` 改为 `JsonResponse<T>`。
- 成功、失败、参数错误、认证失败、权限不足等响应工厂方法统一。
- 分页统一放进 `data`，使用自有 `PageResult<T>`。
- 不再暴露 MyBatis Plus `IPage`。

异常处理：

- `BusinessException` 不再包装完整 `JsonResponse`。
- `BusinessException` 携带错误码、消息和可选上下文。
- `GlobalException` 统一处理参数校验、业务异常、认证异常、权限异常、文件异常、未知系统异常。
- 对外保持兼容响应，对内记录详细日志。

日志切面：

- 保留 `@Log` 操作日志能力。
- 请求日志和操作日志职责拆分。
- 避免记录密码、token、验证码、文件内容等敏感字段。
- 操作日志字段和新版 DDL 同步调整。
- 异步日志失败不影响主流程。

重复提交：

- 保留 `@RepeatInvoke` 思路。
- 重新审查 Redis lock 的 key 生成、过期时间、释放安全性、异常兜底。
- 防重复提交优先设计为幂等 key 或短期请求锁。
- `RedisLock` 不作为通用分布式锁对外承诺，除非后续引入 Redisson 并补测试。

JSON 与时间：

- 统一使用 Jackson。
- 移除 Gson/Fastjson 在业务代码中的使用。
- 时间类型统一使用 `LocalDateTime`。
- Jackson 时间格式由配置统一控制。
- `DateUtils` 中大量 `java.util.Date` 方法进入清理候选。

配置：

- `application.yml` 只放默认无敏感配置。
- profile 配置不提交真实密码。
- DB、Redis、JWT secret、文件路径通过环境变量或本地 ignored 配置提供。
- 默认 profile 不应连接真实数据库。
- Actuator 暴露端点收紧，Prometheus 可保留。

文件能力：

- 保留基础上传下载。
- 强化路径校验、防目录穿越、文件名规范、大小限制、content-type 策略。
- 删除 Office/PDF、二维码、隐写/水印、关键词识别能力。

代码规范：

- 统一构造器注入或 Lombok `@RequiredArgsConstructor`。
- 减少字段注入。
- 统一 Jakarta 注解。
- 清理无用注释、过时包、重复实体、拼写错误。
- Controller 参数绑定方式统一，明确 `@RequestBody`、query、form 的使用边界。

### 持久层解耦与 JPA 迁移计划

第一阶段目标：

- Controller 不再返回或感知 MyBatis Plus 的 `IPage`。
- Service 接口不再继承 MyBatis Plus 的 `IService<T>`。
- Service 方法不再暴露 `IPage`、`Page`、`QueryWrapper`。
- 引入项目自己的分页请求和响应模型，例如 `PageQuery`、`PageResult<T>`。
- 查询条件使用业务 DTO 或 query object。
- 保留 Mapper/MyBatis XML 作为内部实现细节。
- 清理重复实体包，移除 `com.admin.base.system` 旧实体。
- 删除 `tb_keys`、`tb_records` 对应 entity、dto、service、mapper、XML、controller。

第二阶段目标：

- 引入 `spring-boot-starter-data-jpa`。
- 每个核心实体建立对应 Spring Data JPA Repository。
- 删除 MyBatis Plus 依赖、Mapper 接口、Mapper XML、MyBatis 配置。
- JPA 实体使用 Jakarta Persistence 注解。
- 审计字段使用 `@CreatedDate`、`@LastModifiedDate` 或统一基类。
- 事务边界统一放在 service 层。

JPA 落地风格：

- 不一开始建立复杂双向关系。
- 管理员-角色、角色-权限优先保留显式关联实体，例如 `AdminRole`、`RolePermission`。
- 查询权限树、角色权限列表时，用 repository 查询和 service 组装。
- 谨慎使用级联删除，避免误删角色或权限。
- DTO 与实体严格分离。
- 不直接把 JPA entity 返回给前端。
- 避免依赖 Open Session in View。

迁移顺序：

1. 清理接口层 MyBatis Plus 类型。
2. 建立自有分页模型。
3. 建立 repository 边界接口或 query service。
4. 保持 MyBatis 实现跑通。
5. 补齐模块测试。
6. 第二阶段逐模块替换为 JPA Repository。
7. 删除 MyBatis Plus 和 XML。
8. 执行 DDL 和种子数据重建。

优先迁移模块：

1. 全局配置。
2. 操作日志。
3. 角色和权限。
4. 管理员和认证。

### 工具能力移除计划

移除范围：

- Office/PDF 相关能力。
- 二维码相关能力。
- 隐写/水印相关能力。
- 关键词识别相关能力。
- `tb_keys`、`tb_records` 相关业务。
- 对应依赖、实体、DTO、service、controller、mapper、XML、SQL。

保留范围：

- 认证授权。
- 用户、角色、权限。
- 全局配置。
- 操作日志。
- 统一响应。
- 异常处理。
- 缓存。
- 基础文件上传下载。
- 验证码。

## 5. 数据库设计原则

数据库设计以 MySQL 为唯一目标数据库。新版 DDL 不考虑跨数据库兼容，不创建外键。这样可以保留当前团队对 MySQL 的运维习惯，同时避免外键在历史数据清理、种子数据重建和权限菜单调整时带来额外复杂度。

不创建外键并不代表不约束数据一致性。数据一致性通过以下方式保障：

- 组合唯一索引。
- 查询索引。
- service 层业务校验。
- 种子数据校验脚本。
- repository/mapper 测试。
- controller/service 集成测试。

核心表需要在 DDL 草案中逐表审查字段类型、长度、默认值、索引、注释、状态枚举和时间字段。`tb_keys` 和 `tb_records` 不进入新版 DDL。

## 6. 认证授权设计

认证授权采用“默认 JWT，预留 OAuth2/OIDC”的结构。JWT 模式完整可用，OAuth2/OIDC 第一阶段只预留边界。

JWT 模式保留现有登录入口和 Redis 单点登录能力。现有 token filter 当前在 Security 过滤链中使用，不能简单删除。它需要被治理为清晰的 JWT 认证过滤器，并将 CORS、JSON 序列化、错误响应、Redis token 校验等职责拆清楚。

OAuth2/OIDC 模式未来用于接入外部统一认证中心。本项目在该模式下更适合作为 Resource Server，负责校验 Bearer Token、解析用户身份和权限。本项目不在第一阶段自建 Authorization Server。

两种模式共享当前用户、角色、权限模型，但登录入口和 token 校验方式分开。切换通过配置和条件装配在启动时完成。

## 7. 公共组件设计

公共组件保持前端契约兼容，但内部做规范化。

统一返回继续使用 `{code,msg,data}`。分页、错误码、异常处理和认证失败响应都应走统一模型。业务代码不应直接拼零散响应，也不应让底层框架分页对象穿透到接口层。

日志切面、请求日志、重复提交和 Redis lock 需要明确边界。操作日志用于业务操作审计，请求日志用于访问观察。敏感字段必须脱敏或不记录。

JSON 序列化统一 Jackson。Gson/Fastjson 不再作为业务依赖。时间类型统一 `LocalDateTime`，格式由 Spring Boot/Jackson 配置集中管理。

## 8. 测试策略

测试策略是先补最小安全网，再边改边补完整测试。

改造前最小基线：

- 应用上下文能启动。
- `/open/captchaImage` 返回兼容结构。
- `/open/login` 覆盖验证码错误、账号错误、成功登录。
- 无 token 访问受保护接口返回兼容错误结构。
- 有 mock 权限访问核心接口能通过。
- 无权限访问接口被拒绝。
- `JsonResponse` 成功/失败结构稳定。
- `GlobalException` 对参数校验、业务异常、权限异常返回稳定。
- 管理员、角色、权限、配置、日志核心接口至少有 smoke test。

模块改造时同步补：

- Service 单元测试。
- Controller MockMvc 测试。
- Repository/Mapper 测试。
- 数据库 DDL 和种子数据校验。

测试环境：

- `test` profile 不再连接真实 MySQL/Redis。
- Redis 优先使用 mock/fake，必要时使用 Testcontainers Redis。
- MySQL 相关测试优先评估 Testcontainers MySQL。
- 纯 service 测试尽量不用完整 `@SpringBootTest`。
- 安全链路和接口测试使用 MockMvc。
- CI 至少执行 `mvn test` 和必要 profile 检查。

## 9. 风险与开放问题

### 风险

- Spring Boot 3.5 社区支持已在 2026-06-30 结束，但社会采用度较高。
- JPA 迁移可能引入懒加载、事务边界、分页排序、N+1 查询等问题。
- 当前测试基线薄弱，改造前必须先补最小安全网。
- 删除专用工具能力会减少模板内置功能，需要确认没有外部调用依赖。
- 不建外键会要求 service 层和测试承担更多一致性责任。
- OAuth2/OIDC 预留结构如果过度设计，会增加第一阶段复杂度。

### 开放问题

- Druid 是否保留，还是切换回 Spring Boot 默认 HikariCP。
- Java 是否长期保持 17，或未来升级到 Java 21。
- Spring Boot 3.5.x 的具体补丁版本在实施时再次确认。
- 操作日志 `status` 最终定义为 `success`、`status_code`，还是拆分两个字段。
- 是否引入 Flyway 管理新版 DDL 和种子数据。
- 是否需要保留旧 API 路径别名以兼容前端。

## 10. 后续计划

本文档确认后，下一步不是直接实施代码改造，而是编写实施计划。实施计划需要按阶段和子计划拆分任务，明确每个任务的文件范围、测试要求、验收命令和回滚策略。

建议实施计划顺序：

1. 补最小测试基线。
2. 依赖和配置治理。
3. 统一响应、异常和分页模型。
4. JWT filter 和 Security 配置治理。
5. MyBatis Plus 对外类型解耦。
6. 删除专用工具能力和 `tb_keys`、`tb_records`。
7. MySQL DDL 草案和种子数据脚本。
8. 第二阶段 JPA 迁移计划。

## 11. Phase 1 完成状态

**日期:** 2026-07-09
**状态:** ✅ 已完成

Phase 1 所有 10 个任务已实施并通过验证。详见 [Phase 1 Verification Report](../modernization/phase-1-verification.md)。

### 已交付

| 任务 | 描述 | 状态 |
|------|------|------|
| P1-01 | 基线测试框架 | ✅ |
| P1-02 | Parent POM 与依赖治理 | ✅ |
| P1-03 | 安全配置与 Auth Mode 属性 | ✅ |
| P1-04 | 兼容响应、分页与异常基础设施 | ✅ |
| P1-05 | JWT 安全过滤器治理 | ✅ |
| P1-06 | Service 契约与 MyBatis Plus 解耦 | ✅ |
| P1-07 | 移除 Keys/Records/特殊工具 | ✅ |
| P1-08 | MySQL DDL 与种子数据草案 | ✅ |
| P1-09 | 日志/Redis 锁/敏感字段治理 | ✅ |
| P1-10 | Phase 1 验证与文档 | ✅ |

### 验收结果

- `mvn test`: 27 tests, 0 failures, 0 errors (4 skipped: 需要外部 MySQL/Redis)
- 依赖扫描: 无 fastjson2/gson/poi/zxing/javax.annotation-api
- 类型泄漏: controller 和 service 接口无 IPage/IService/QueryWrapper
- Schema: 无 foreign key, 无 tb_keys/tb_records
- JWT 模式: 默认可执行认证模式
- OAuth2/OIDC: 预留，未实现
