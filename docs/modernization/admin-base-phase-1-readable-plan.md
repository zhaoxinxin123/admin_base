# admin_base 第一阶段改造说明

日期：2026-07-08

本文档仅用于人工阅读，帮助理解第一阶段要做什么、为什么这么做、先后顺序是什么。它不是给自动执行代理使用的详细任务清单。

## 一、改造目标

第一阶段的目标是先把项目“扶稳”，不急着重写，也不急着把 MyBatis Plus 替换成 JPA。

这一阶段重点解决：

- 依赖混乱和历史依赖残留。
- 配置文件中存在真实数据库、Redis 密码。
- 统一返回、异常处理、分页模型不够规范。
- Spring Security + JWT 认证过滤器职责混杂。
- controller/service 暴露了 MyBatis Plus 类型。
- 数据库字段设计需要重新审查。
- `tb_keys`、`tb_records` 以及关键词识别、Office/PDF、二维码、隐写/水印等专用能力需要移除。
- 当前测试太少，改造前需要先建立最小测试安全网。

第一阶段不做：

- 不迁移到 JPA。
- 不接入真实 OAuth2/OIDC 认证中心。
- 不替换 MySQL。
- 不创建数据库外键。
- 不大规模重构包结构。
- 不重写所有 API。

## 二、总体路线

采用“治理先行，迁移后置”的路线。

第一阶段先治理基础设施，让项目变得可测试、可维护、可继续演进。

第二阶段再做 JPA 迁移、包结构模块化、数据库 DDL 实施、OAuth2/OIDC 真实接入等更大的改造。

这样做的原因是：当前项目测试基线弱，依赖和配置也不够干净。如果一开始就同时做 JPA、数据库、认证、包结构，风险会非常高。

## 三、第一阶段任务顺序

### 1. 先补最小测试基线

在改造前先补一组最小测试，作为安全网。

需要覆盖：

- `JsonResponse` 响应结构。
- `/open/captchaImage` 是否保持公开访问。
- 受保护接口在无 token 时是否被拒绝。
- 基础 Spring 上下文能否启动。

现有 `BaseApplicationTests` 比较脆弱，依赖固定验证码和真实缓存状态，后续应替换为更聚焦的测试。

### 2. 整理 `pom.xml`

依赖管理切换为 `spring-boot-starter-parent`。

目标版本暂定为 Spring Boot `3.5.x` 最新补丁线。原因是社会采用度优先，不追 Spring Boot 4.x 最新版本。

需要删除：

- 重复的 MyBatis Plus starter。
- `mybatis-plus-generator`。
- Gson。
- Fastjson。
- POI。
- ZXing。
- Velocity。
- `javax.annotation-api`。
- 其它与 Office/PDF、二维码、隐写/水印、关键词识别相关的依赖。

第一阶段保留 MyBatis Plus 运行能力，但只作为内部实现细节。

### 3. 清理配置文件

配置文件中不能继续提交真实数据库密码、Redis 密码、内网地址。

要改成环境变量形式，例如：

```yaml
spring:
  datasource:
    url: ${DEV_DATASOURCE_URL:jdbc:mysql://127.0.0.1:3306/admin_base}
    username: ${DEV_DATASOURCE_USERNAME:root}
    password: ${DEV_DATASOURCE_PASSWORD:}
```

默认 profile 不应连接真实生产或内网数据库。

新增认证模式配置：

```yaml
admin:
  auth:
    mode: jwt
```

第一阶段只完整支持 `jwt`，`oauth2` 只预留结构。

### 4. 统一响应和异常

保持前端看到的响应结构不变：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {}
}
```

但内部要规范化：

- `JsonResponse` 改为 `JsonResponse<T>`。
- 新增项目自己的 `PageResult<T>`。
- 不再把 MyBatis Plus 的 `IPage` 暴露给 controller 或 service 接口。
- `BusinessException` 不再包装完整 `JsonResponse`，只保存错误码和错误消息。
- `GlobalException` 统一处理业务异常、参数校验异常、认证异常、权限异常和兜底异常。

### 5. 治理 JWT 认证过滤器

当前 `MyTokenFilter` 确实在使用，不能删除。

它在 `SecurityConfig` 中通过：

```java
.addFilterBefore(myTokenFilter(), UsernamePasswordAuthenticationFilter.class)
```

加入了 Spring Security 过滤链。

第一阶段要做的是治理，而不是移除：

- 保留 JWT 模式下的 token filter。
- 去掉 filter 中直接 new Gson 的写法，统一使用 Jackson。
- CORS 不再放在 filter 里处理，改由统一 CORS 配置处理。
- Bearer token 截取、token 失效、Redis 踢下线等逻辑整理清楚。
- Spring Security 注解从旧写法迁移到 Spring Security 6 推荐写法。
- OAuth2/OIDC 模式只预留配置和类边界，不接入真实认证中心。

### 6. 解耦 MyBatis Plus 对外契约

第一阶段不删除 MyBatis Plus，但要把它藏到实现层。

需要做到：

- Service 接口不再继承 `IService<T>`。
- Service 接口不再返回 `IPage<T>`。
- Controller 不再 import 或感知 `IPage`。
- 分页统一返回项目自己的 `PageResult<T>`。
- `QueryWrapper`、`Page` 等 MyBatis Plus 类型只允许出现在实现类内部。

这样第二阶段迁移 JPA 时，controller 和上层业务代码不需要跟着大面积改。

### 7. 移除非核心业务能力

明确移除：

- `tb_keys`
- `tb_records`
- 关键词识别相关代码。
- Office/PDF 相关代码。
- 二维码相关代码。
- 隐写/水印相关代码。
- Boyer 相关工具代码。

这些能力不再作为后台模板核心能力保留。

保留：

- 用户。
- 角色。
- 权限。
- 全局配置。
- 操作日志。
- 认证授权。
- 验证码。
- 基础文件上传下载。
- Redis 缓存。
- 统一响应和异常处理。

### 8. 输出新版 MySQL DDL 草案

数据库继续使用 MySQL。

新版 DDL 只包含核心系统表：

- `tb_sys_admin`
- `tb_sys_role`
- `tb_sys_permissions`
- `tb_sys_admin_role`
- `tb_sys_role_permission`
- `tb_sys_global_config`
- `tb_sys_operation_log`

明确不包含：

- `tb_keys`
- `tb_records`

DDL 规则：

- 不建外键。
- 使用主键、唯一索引、普通索引。
- 关联一致性由 service 层和测试保证。
- 状态字段统一用 `tinyint`。
- 主键类型统一，不再混用 `int(11)`、`int(20)`、`bigint(20)`。
- 删除 `password_show`。
- 操作日志请求参数和响应结果使用 `json` 或大字段，不再用 `varchar(255)`。

### 9. 日志和敏感字段治理

操作日志和请求日志要区分职责。

日志中不能记录：

- 密码。
- token。
- 验证码。
- 文件内容。
- 其它敏感字段。

`@Log` 可以保留，但需要补充脱敏策略。

`RedisLock` 只作为防重复提交的短期锁，不对外承诺为通用分布式锁框架。

### 10. 第一阶段验收

第一阶段完成后，需要满足：

- `mvn test` 能通过，或者明确记录缺失的外部依赖。
- `pom.xml` 已经切到 `spring-boot-starter-parent`。
- 项目中不再依赖 Gson、Fastjson、POI、ZXing、`javax.annotation-api` 等移除项。
- controller 和 service 接口中不再暴露 MyBatis Plus 类型。
- `tb_keys`、`tb_records` 已从新版 DDL 草案中移除。
- 新版 DDL 不包含外键。
- JWT 模式仍可运行。
- OAuth2/OIDC 仍只是预留，不在第一阶段强行实现。
- 基础种子数据策略清楚。

## 四、建议提交节奏

建议按以下顺序小步提交：

1. `test: add modernization baseline tests`
2. `build: simplify spring boot dependency management`
3. `chore: externalize runtime configuration`
4. `refactor: standardize response and exception infrastructure`
5. `refactor: govern jwt security filter`
6. `refactor: hide mybatis plus from service contracts`
7. `refactor: remove non-core tool features`
8. `docs: add mysql schema draft for system tables`
9. `refactor: sanitize operation logging`
10. `docs: add phase 1 verification checklist`

每个提交都应该可以单独 review，不要把多个大方向混在一个提交里。

## 五、后续阶段

第一阶段完成后，再进入第二阶段计划。

第二阶段重点是：

- Spring Data JPA 替换 MyBatis Plus。
- 删除 Mapper 和 XML。
- 实施新版 DDL。
- 整理包结构。
- 决定是否保留 Druid。
- 决定是否升级 Java 21。
- 接入真实 OAuth2/OIDC 认证中心。

第二阶段不应该在第一阶段过程中提前混入。

