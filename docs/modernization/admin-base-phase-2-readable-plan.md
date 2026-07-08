# admin_base 第二阶段改造说明：JPA 与数据库迁移

日期：2026-07-08

本文档仅用于人工审核，帮助理解第二阶段要做什么、为什么这么做、风险在哪里。它不是自动执行代理使用的详细任务清单。

## 一、阶段目标

第二阶段的目标是：在第一阶段已经完成基础治理、测试基线、响应结构规范、配置清理、MyBatis Plus 对外解耦之后，把内部持久层实现从 MyBatis Plus 迁移到 Spring Data JPA。

这一阶段完成后，项目中应不再包含：

- MyBatis Plus 依赖。
- Mapper 接口。
- Mapper XML。
- MyBatis Plus 分页和查询 wrapper。
- MyBatis 配置。

同时要保留：

- 现有 API 基本兼容。
- `{code,msg,data}` 响应结构。
- Spring Security + JWT 默认认证模式。
- MySQL 数据库。
- 不建外键的 DDL 规则。

## 二、前置条件

第二阶段不能直接从当前旧代码开始，必须等第一阶段完成后再做。

必须先满足：

- service 接口不再继承 `IService<T>`。
- controller 和 service 接口不再暴露 `IPage<T>`。
- 分页已经统一为项目自己的 `PageResult<T>`。
- `tb_keys`、`tb_records` 和专用工具能力已经移除。
- 测试基线已经建立。
- 新版 MySQL DDL 草案已经存在。

否则直接迁移 JPA 会让改动过大，风险很高。

## 三、总体策略

第二阶段采用“逐模块替换”的方式，不一次性删除全部 MyBatis。

迁移顺序建议：

1. 全局配置模块。
2. 操作日志模块。
3. 角色和权限模块。
4. 管理员和认证相关持久层。
5. 删除 MyBatis Plus 和 Mapper/XML。
6. 校验新版 DDL 与种子数据。

这样排序的原因：

- 全局配置最简单，适合作为 JPA 试点。
- 操作日志主要验证分页、排序和大字段。
- 角色权限关系复杂，需要等基础模式稳定后再处理。
- 管理员和认证最敏感，放在最后迁移。

## 四、JPA 实体设计原则

JPA 落地采用保守风格。

不建议一开始使用大量复杂关系：

- 不做复杂双向关联。
- 不直接使用 `@ManyToMany` 表达管理员-角色、角色-权限。
- 不使用大范围 `CascadeType.REMOVE`。
- 不依赖 Open Session in View。
- 不把 JPA entity 直接返回给前端。

保留显式关联实体：

- `AdminRole`
- `RolePermission`

也就是说，管理员和角色之间仍然通过 `AdminRole` 关系表表达，角色和权限之间仍然通过 `RolePermission` 表表达。

Service 层负责查询和组装业务结果。

## 五、主键与字段类型

新版实体主键建议使用 `Long`，对应 MySQL 中的 `BIGINT`。

但为了保持接口兼容，请求 DTO 中已有的 `Integer` id 不需要立刻全部改掉。

建议做法：

- Controller 和请求参数暂时保持兼容。
- Service 内部把 `Integer` 转为 `Long`。
- 后续如果要统一 API 类型，再单独做兼容升级。

这样可以避免第二阶段同时触碰前端契约。

## 六、Repository 设计

每个核心表建立一个 Spring Data JPA Repository：

- `AdminRepository`
- `RoleRepository`
- `PermissionsRepository`
- `AdminRoleRepository`
- `RolePermissionRepository`
- `GlobalConfigRepository`
- `OperationLogRepository`

Repository 只负责数据访问，不承载业务规则。

复杂业务逻辑仍放在 Service 层，例如：

- 校验用户名是否重复。
- 创建管理员后绑定角色。
- 删除权限时同步删除角色权限关系。
- 构建权限树。
- 分页查询日志。

## 七、模块迁移说明

### 1. 全局配置模块

这是 JPA 试点模块。

迁移内容：

- `GlobalConfigServiceImpl` 不再继承 MyBatis Plus `ServiceImpl`。
- 改为构造器注入 `GlobalConfigRepository`。
- `selectByKey` 使用 `findByConfigKey`。
- 分页使用 Spring Data 的 `Pageable`。
- 返回项目自己的 `PageResult<GlobalConfig>`。

验收重点：

- 新增配置。
- 根据 key 查询配置。
- 分页查询配置。
- key 唯一性校验。

### 2. 操作日志模块

操作日志用于验证分页、排序和日志字段。

迁移内容：

- `OperationLogServiceImpl` 改用 `OperationLogRepository`。
- 插入日志使用 `save`。
- 分页查询使用 `PageRequest`。
- 默认按 `operationTime` 倒序。

验收重点：

- 日志写入不影响主流程。
- 分页结构仍然兼容。
- 日志查询排序正确。

### 3. 角色和权限模块

这是关系复杂模块。

迁移内容：

- `RoleServiceImpl` 改用 `RoleRepository`。
- `PermissionsServiceImpl` 改用 `PermissionsRepository`。
- `RolePermissionServiceImpl` 改用 `RolePermissionRepository`。
- 权限树仍由 Service 在内存中组装。
- 更新角色权限时，采用“删除旧关系，再插入新关系”的显式策略。

验收重点：

- 角色新增、修改、删除。
- 权限新增、修改、删除。
- 角色权限分配。
- 权限树构建。
- 权限字符串仍可用于 `@PreAuthorize`。

### 4. 管理员和认证持久层

这是最敏感模块，放在后面迁移。

迁移内容：

- `AdminServiceImpl` 改用 `AdminRepository`。
- `AdminRoleServiceImpl` 改用 `AdminRoleRepository`。
- `UserDetailsServiceImpl` 改用 JPA 查询用户、角色、权限。
- 登录和权限加载逻辑保持对外行为不变。

验收重点：

- 管理员登录。
- 密码校验。
- 管理员新增。
- 管理员删除。
- 修改密码。
- 修改状态。
- 分配角色。
- 根据用户加载权限。

## 八、删除 MyBatis Plus

只有在所有模块都迁移完成并通过测试后，才能删除 MyBatis Plus。

删除范围：

- `pom.xml` 中 MyBatis Plus 依赖。
- `src/main/java/com/admin/base/mapper/`
- `src/main/resources/mapper/`
- `MybatisInterceptor`
- `application*.yml` 中的 `mybatis-plus` 配置。

删除后要扫描：

```bash
rg -n "com.baomidou.mybatisplus|mybatis-plus|QueryWrapper|BaseMapper|IPage|IService" pom.xml src/main/java src/main/resources
```

期望结果：没有匹配。

## 九、新版 DDL 和种子数据校验

第二阶段要确保 JPA 实体与新版 MySQL DDL 保持一致。

新版 DDL 必须满足：

- 只包含系统核心表。
- 不包含 `tb_keys`。
- 不包含 `tb_records`。
- 不包含外键。
- 保留唯一索引和普通索引。
- 主键、状态字段、时间字段类型与实体匹配。

种子数据只保留：

- 管理员。
- 角色。
- 权限菜单。
- 基础配置。

不迁移：

- 历史操作日志。
- 关键词识别数据。
- 其它专用业务数据。

## 十、风险点

第二阶段主要风险：

- JPA 懒加载导致运行时异常。
- 分页字段和排序字段与数据库列不一致。
- `Integer` 和 `Long` id 转换遗漏。
- 角色权限查询出现 N+1 查询。
- 删除关系数据时误删主表数据。
- 操作日志 JSON 字段和实体字段映射不一致。
- 测试数据库与真实 MySQL 行为不一致。

规避方式：

- 不依赖 Open Session in View。
- 不使用复杂双向关系。
- 不使用大范围级联删除。
- 逐模块迁移，逐模块测试。
- 使用 Testcontainers MySQL 做关键 repository 测试。

## 十一、第二阶段验收标准

第二阶段完成后，需要满足：

- `mvn test` 通过。
- MyBatis Plus 依赖已移除。
- Mapper 接口已移除。
- Mapper XML 已移除。
- MyBatis 配置已移除。
- 系统核心模块使用 Spring Data JPA Repository。
- controller 和 service 对外契约保持基本兼容。
- 新版 DDL 不包含外键。
- 新版 DDL 不包含 `tb_keys`、`tb_records`。
- 管理员、角色、权限、配置、日志功能测试通过。

## 十二、第二阶段之后

第二阶段完成后，项目的持久层已经完成现代化。

接下来可以进入第三阶段：

- 接入 OAuth2/OIDC。
- 让项目支持 `jwt` 和 `oauth2` 两种认证模式。
- 对接外部统一认证中心。

