# OAuth2/OIDC Provider Setup

## Runtime Mode

Set:

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

## Token Requirements

- Token must be a JWT accepted by Spring Security Resource Server.
- Token issuer must match `admin.oauth2.issuer-uri`.
- Token audience must include `admin.oauth2.audience`.
- Token should contain a username claim.
- Token should contain an authorities claim with permission strings such as `sys:adminList`.

## Responsibility Split

- External provider handles login, SSO session, password policy, MFA, and token issuance.
- `admin_base` validates bearer tokens and enforces API permissions.
- Local `/open/login` remains for JWT mode.
