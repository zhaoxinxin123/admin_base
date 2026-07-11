# Phase 3 Verification

## Required Commands

- `mvn test`
- `mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2AudienceValidatorTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest`
- `rg -n "ADMIN_AUTH_MODE|ADMIN_OAUTH2|admin.auth.mode|admin.oauth2" src/main/resources docs/modernization`

## Expected Results

- JWT mode remains default.
- OAuth2 mode loads the Resource Server security chain.
- OAuth2 JWT authorities map to existing permission strings.
- OAuth2 JWT issuer and audience are validated.
- Provider setup documentation exists.
- The project does not issue OAuth2 tokens.
