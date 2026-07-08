# admin_base 第三阶段改造说明：OAuth2/OIDC 接入

日期：2026-07-08

本文档仅用于人工审核，帮助理解第三阶段 OAuth2/OIDC 接入方案。它不是自动执行代理使用的详细任务清单。

## 一、阶段目标

第三阶段的目标是：在保留本地 JWT 模式的前提下，增加 OAuth2/OIDC Resource Server 模式。

最终项目支持两种认证模式：

```yaml
admin:
  auth:
    mode: jwt
```

或：

```yaml
admin:
  auth:
    mode: oauth2
```

切换方式是启动时切换，不做运行时热切换。

## 二、核心结论

Spring Security OAuth2 Resource Server 本身不负责单点登录页面，也不负责账号密码登录和发 token。

单点登录由外部认证中心负责，例如：

- Keycloak
- Casdoor
- Auth0
- Okta
- 企业内部 OIDC 平台
- 其它支持 OAuth2/OIDC 的统一认证中心

本项目在 OAuth2 模式下只做资源服务：

- 校验 Bearer Token。
- 解析 token 中的用户信息。
- 解析 token 中的权限。
- 执行接口权限控制。

本项目不在第三阶段变成 Authorization Server。

## 三、保留 JWT 模式

JWT 模式仍然是默认模式。

保留：

- `/open/login`
- 验证码登录。
- 本地用户名密码校验。
- 本地签发 JWT。
- Redis 单点登录和踢下线。
- 当前用户、角色、权限模型。

这样做的好处：

- 单系统部署仍然简单。
- 不依赖外部认证中心。
- 旧前端和旧部署方式仍可继续使用。

## 四、OAuth2/OIDC 模式

OAuth2/OIDC 模式通过配置启用：

```yaml
admin:
  auth:
    mode: oauth2
  oauth2:
    issuer-uri: https://your-provider/realms/admin-base
    audience: admin-api
    username-claim: preferred_username
    authorities-claim: authorities
```

在该模式下：

- 前端不再调用本项目 `/open/login` 完成登录。
- 前端先跳转外部认证中心登录。
- 登录成功后拿到 access token。
- 前端调用本项目接口时携带 `Authorization: Bearer <token>`。
- 本项目校验 token，并根据 claims 生成权限。

## 五、安全配置拆分

第三阶段需要把安全配置拆成两套：

1. `JwtSecurityConfig`
2. `OAuth2ResourceServerSecurityConfig`

JWT 模式启用：

- 本地 JWT token filter。
- 本地登录接口。
- Redis token 校验。

OAuth2 模式启用：

- Spring Security Resource Server。
- JWT decoder。
- claims 到 authorities 的映射。
- 不启用本地 JWT token filter。

两套配置通过 `admin.auth.mode` 进行条件装配。

## 六、权限映射

当前项目权限控制主要依赖：

```java
@PreAuthorize("hasAuthority('sys:adminList')")
```

第三阶段要继续兼容这种权限模型。

OAuth2 token 中需要包含权限 claim，例如：

```json
{
  "preferred_username": "admin",
  "authorities": [
    "sys:adminList",
    "sys:roleList",
    "sys:permission"
  ]
}
```

项目会读取 `authorities` claim，并转换为 Spring Security 的 `GrantedAuthority`。

如果外部认证中心的 claim 名不叫 `authorities`，可以通过配置修改：

```yaml
admin:
  oauth2:
    authorities-claim: permissions
```

## 七、当前用户抽象

为了让 JWT 模式和 OAuth2 模式共用业务代码，需要抽象当前用户获取方式。

建议新增：

- `CurrentUser`
- `CurrentUserProvider`
- `SecurityContextCurrentUserProvider`

业务代码不直接依赖 `SecurityContextHolder`。

这样两种认证模式都可以统一获取：

- 用户名。
- 管理员 ID。
- 权限列表。

需要注意：

- JWT 模式可以从本地 `UserDetailsImpl` 中拿到 `adminId`。
- OAuth2 模式的 token 未必有本地 `adminId`。
- 如果需要本地管理员 ID，需要额外做账号绑定策略。

账号绑定策略可以后续单独设计，不建议在第三阶段一开始复杂化。

## 八、外部认证中心职责

外部认证中心负责：

- 登录页面。
- 用户密码校验。
- 单点登录会话。
- 多因素认证。
- token 签发。
- token 刷新。
- 用户在多个系统间共享登录状态。

本项目负责：

- 校验 token。
- 解析权限。
- 保护 API。
- 根据权限执行接口访问控制。

这个职责边界必须清楚，否则容易把 Resource Server 和 Authorization Server 混在一起。

## 九、JWT 与 OAuth2 的关系

本地 JWT 模式和 OAuth2 模式都可能使用 JWT 格式的 token，但来源不同。

本地 JWT 模式：

- token 由本项目签发。
- Redis 保存 token，用于单点登录和踢下线。
- 本项目负责登录和 token 生命周期。

OAuth2 模式：

- token 由外部认证中心签发。
- 本项目只校验 token。
- 单点登录由外部认证中心负责。
- token 失效、刷新、踢下线主要由认证中心控制。

因此两者不能混在同一个 filter 里处理，应该拆成两套安全链。

## 十、需要新增的配置

建议新增：

```yaml
admin:
  oauth2:
    issuer-uri: ${ADMIN_OAUTH2_ISSUER_URI:}
    audience: ${ADMIN_OAUTH2_AUDIENCE:admin-api}
    username-claim: ${ADMIN_OAUTH2_USERNAME_CLAIM:preferred_username}
    authorities-claim: ${ADMIN_OAUTH2_AUTHORITIES_CLAIM:authorities}
```

含义：

- `issuer-uri`：外部认证中心 issuer 地址。
- `audience`：本系统 API 对应的受众标识。
- `username-claim`：用户名 claim 名。
- `authorities-claim`：权限 claim 名。

## 十一、测试重点

第三阶段需要重点测试：

- 默认配置仍然启用 JWT 模式。
- `admin.auth.mode=oauth2` 时启用 OAuth2 Resource Server。
- OAuth2 token 中的权限能映射为 `GrantedAuthority`。
- 带权限 token 能访问受保护接口。
- 无权限 token 会被拒绝。
- JWT 模式下原有登录仍可用。
- 项目不会在 OAuth2 模式下尝试自己签发 OAuth2 token。

## 十二、接入文档

第三阶段应补充一份外部认证中心接入说明。

文档需要说明：

- 如何配置 `issuer-uri`。
- token 里需要哪些 claim。
- 权限字符串如何映射。
- 本项目和认证中心的职责边界。
- 本项目不负责 OAuth2 登录页和 token 签发。

## 十三、风险点

主要风险：

- 外部认证中心 token claim 格式不统一。
- 权限 claim 名称和项目配置不一致。
- token 中只有角色，没有细粒度权限。
- OAuth2 模式下缺少本地 `adminId`。
- 前端需要改登录流程。
- token 即时失效依赖认证中心能力，和当前 Redis 踢下线机制不同。

规避方式：

- claim 名称做成可配置。
- 权限映射单独封装。
- 当前用户获取做抽象。
- 不在第一版 OAuth2 接入中强制做本地账号绑定。
- 保留 JWT 模式作为默认模式。

## 十四、第三阶段验收标准

第三阶段完成后，需要满足：

- JWT 模式仍然可用，并且仍是默认模式。
- OAuth2 模式可以通过配置启用。
- OAuth2 模式使用 Spring Security Resource Server。
- token claims 可以映射到当前权限字符串。
- `@PreAuthorize` 权限校验继续有效。
- 项目不承担 Authorization Server 职责。
- 外部认证中心接入文档完成。
- 测试覆盖权限映射和两种模式的配置切换。

