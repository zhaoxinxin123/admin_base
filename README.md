# admin_base

AI 使用的 Java 后台脚手架。

这是一个基于 Java 17 和 Spring Boot 的后台管理基础项目，包含认证授权、角色权限、全局配置、操作日志、验证码、Redis 缓存、文件基础能力、统一响应和异常处理等常见后台能力。项目当前仍保留 MyBatis Plus 实现，后续按规划逐步治理依赖、配置、测试、数据库结构，并迁移到 Spring Data JPA。

## 技术栈

- Java 17
- Spring Boot 3.x
- Spring Security
- MyBatis Plus
- MySQL
- Redis
- Maven
- JUnit 5 / MockMvc
- Actuator / Prometheus

## 项目结构

```text
src/main/java/com/admin/base
├── annotation      # 自定义注解
├── asp             # AOP 切面
├── common          # 统一响应
├── component       # 通用组件
├── config          # Spring、Security、Redis、MyBatis 等配置
├── constant        # 常量和枚举
├── controller      # REST 接口
├── dto             # 请求和响应 DTO
├── entity          # 数据实体
├── exception       # 业务异常和全局异常处理
├── filter          # 认证过滤器
├── mapper          # MyBatis Mapper
├── service         # 业务服务
└── utils           # 工具类
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

## 认证流程

当前默认认证模式是 Spring Security + 本地 JWT：

1. `GET /open/captchaImage` 获取验证码。
2. `POST /open/login` 登录并返回 token。
3. 请求业务接口时携带 `Authorization: Bearer <token>`。
4. Redis 用于 token 校验、单点登录和踢下线。
5. Controller 使用 `@PreAuthorize` 校验权限字符串。

后续规划中会保留本地 JWT 模式，并预留 OAuth2/OIDC Resource Server 模式。

## 数据库

当前数据库为 MySQL，现有脚本位于：

```text
src/main/resources/base_sql_v1.0.sql
```

后续改造原则：

- 继续使用 MySQL。
- DDL 不创建外键。
- 通过主键、唯一索引、普通索引、Service 校验和测试保证一致性。
- 移除 `tb_keys`、`tb_records` 等非核心模板表。
- 核心保留系统管理表、角色权限表、全局配置表和操作日志表。

## 改造规划文档

项目中已保留三阶段中文改造说明：

- [第一阶段：基础治理](docs/modernization/admin-base-phase-1-readable-plan.md)
- [第二阶段：JPA 与数据库迁移](docs/modernization/admin-base-phase-2-readable-plan.md)
- [第三阶段：OAuth2/OIDC 接入](docs/modernization/admin-base-phase-3-readable-plan.md)

详细执行计划位于：

```text
docs/superpowers/plans/
```

总体设计文档位于：

```text
docs/superpowers/specs/2026-07-08-admin-base-modernization-design.md
```

## 当前状态

当前版本是旧项目重新初始化后的基线版本，已包含改造规划文档。后续建议按阶段和任务拆分 PR，小步提交、逐步验证。

