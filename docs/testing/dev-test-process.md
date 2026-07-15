# dev 环境测试流程

本文档用于规范 `admin_base` 在 dev profile 下的白盒与接口测试流程。测试不得启动本地 MySQL 或 Redis。

## 1. 测试环境

- Spring profile：`dev`
- MySQL：默认 `192.168.3.3:3306/admin_base_it`（可清空的一次性测试库）
- Redis：默认 `192.168.3.3:6379`
- 默认账号：`admin / 123456`

如测试环境地址或密码变化，使用环境变量覆盖：

```bash
export DEV_DATASOURCE_URL='jdbc:mysql://192.168.3.3:3306/admin_base_it?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false'
export DEV_DATASOURCE_USERNAME=root
export DEV_DATASOURCE_PASSWORD='hjdz@10086'
export DEV_REDIS_HOST='192.168.3.3'
export DEV_REDIS_PORT=6379
export DEV_REDIS_PASSWORD='hjdz@10086'
```

## 2. 数据准备

先创建专用数据库并依次导入 schema、seed：

```bash
mysql -h192.168.3.3 -uroot -p -e "CREATE DATABASE IF NOT EXISTS admin_base_it CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -h192.168.3.3 -uroot -p admin_base_it < docs/database/admin-base-schema-v2.sql
mysql -h192.168.3.3 -uroot -p admin_base_it < docs/database/admin-base-seed-v2.sql
```

`FullApiAuthorizationIntegrationTest` 会在每个测试前清空并恢复 7 张核心表。测试启动时会解析 JDBC URL；数据库名不以 `_it` 结尾时立即失败，禁止清空 `admin_base` 等业务库。

- `tb_sys_admin`
- `tb_sys_role`
- `tb_sys_permissions`
- `tb_sys_admin_role`
- `tb_sys_role_permission`
- `tb_sys_global_config`
- `tb_sys_operation_log`

恢复顺序为执行 `docs/database/admin-base-seed-v2.sql`，再创建仅有 `sys:adminList` 权限的用户 `limited / 123456`。

## 3. 覆盖矩阵

| 范围 | 测试类 | 覆盖内容 |
| --- | --- | --- |
| Schema/Seed | `SchemaDraftTest`, `SchemaSeedConsistencyTest`, `SeedV2LogicCoverageTest` | SQL 脚本、默认数据、权限映射、seed 驱动接口 |
| JWT 安全 | `SecurityConfigTest`, `SecurityBoundaryTest`, `OpenEndpointTest` | 公开端点、匿名拒绝、无效 token |
| OAuth2 | `AuthModeSecurityConfigTest`, `OAuth2*Test`, `OAuth2ResourceServerTest` | auth mode 切换、真实 RSA 签名与 JWKS discovery、issuer/audience、权限 claim、Bearer token 资源服务器链路 |
| 全接口白盒 | `FullApiAuthorizationIntegrationTest` | 管理员全接口、普通用户权限边界、操作日志、上传下载、成功与异常路径 |
| 架构边界 | `PackageStructureBoundaryTest`, `PersistenceBoundaryTest`, `RemovedFeatureBoundaryTest` | 包结构、JPA 边界、已移除技术栈 |
| 公共组件 | `JsonResponseTest`, `PageResultTest`, `BusinessExceptionTest`, `JwtTokenUtilTest`, `LogAspectSanitizationTest` | 响应模型、分页、异常、JWT、日志脱敏 |

当前全接口白盒测试覆盖：

- 公开接口：验证码、登录成功、验证码错误、密码错误。
- 管理员：查询、菜单、新增、删除、重置密码、状态更新、角色分配、重复账号、删除自己。
- 角色：查询、新增、更新、删除、非法角色名。
- 权限：查询、新增、更新、删除、非法父级。
- 全局配置：查询、新增、更新、删除、重复 key。
- 操作日志：查询、删除、异步日志完整性、敏感参数脱敏。
- 文件：上传、下载、资源下载、下载字节和响应头、下载后删除、缺失文件错误。
- 权限边界：`admin` 可操作全部接口；`limited` 只能访问公开接口、当前用户菜单和已授权的管理员列表，其他查询、文件和写操作均返回业务 `code=401`。

## 4. 执行命令

先执行格式检查：

```bash
git diff --check
```

执行聚焦测试：

```bash
mvn test -Dtest=FullApiAuthorizationIntegrationTest
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest
mvn test -Dtest=SchemaDraftTest,SchemaSeedConsistencyTest,SeedV2LogicCoverageTest
```

执行全量测试：

```bash
mvn test
```

## 5. 通过标准

- `git diff --check` 无输出。
- 所有 Maven 测试 `Failures: 0, Errors: 0`。
- 全接口白盒测试确认管理员、普通用户、公开接口、错误路径和操作日志均符合预期。
- Apifox 导入文件 `docs/api/admin-base-openapi.apifox.yaml` 与当前 controller 路由保持一致。

## 6. Apifox 导入

在 Apifox 中选择“导入数据 -> OpenAPI/Swagger”，导入：

```text
docs/api/admin-base-openapi.apifox.yaml
```

导入后配置环境变量：

- `baseUrl = http://localhost:9999/admin-api`
- `token = 登录接口返回的 data.token`

除 `/open/**` 外，其余接口在 Authorization 中使用：

```text
Bearer {{token}}
```
