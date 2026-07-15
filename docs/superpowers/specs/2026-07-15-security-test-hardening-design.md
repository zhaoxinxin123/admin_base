# Security And Test Hardening Design

## Scope

This design fixes review findings 2, 4, 5, 6, 7, 8, and 9:

- isolate destructive API tests from the shared `admin_base` database;
- require explicit permissions for file upload, download, and deletion;
- make OAuth2 username mapping, operation logging, and signed-token validation usable;
- verify every protected endpoint against its exact authority;
- make download failures observable and assert actual response bytes;
- prevent permission-tree cycles and recursively remove descendants;
- enforce role-name prefix, length, and uniqueness rules in the service layer.

Captcha exposure, credential rotation, and production default configuration are outside this change.

## Test Database Isolation

Destructive integration tests use the dedicated remote database `admin_base_it` on `192.168.3.3`. A reusable database guard reads the JDBC URL and refuses destructive setup unless the database name ends with `_it`. The full API test initializes schema and seed data only inside that database and serializes its reset operation so concurrent test methods in one JVM cannot race.

Ordinary integration tests remain read-only or transaction-scoped. Documentation will state that `admin_base_it` is disposable and that `admin_base` must never be truncated by tests.

## File Permissions

The seed gains explicit authorities for file upload, download, and deletion. The administrator role receives all three. File endpoints use `hasAuthority` checks; deletion additionally requires the delete authority when `delete=true`. A limited user with only system query permissions receives no file access.

The controller stops swallowing download exceptions. Invalid names and missing files produce the existing structured business-error response. Successful tests assert bytes, media type, attachment headers, and deletion behavior.

## OAuth2

`JwtAuthenticationConverter` applies `OAuth2Properties.usernameClaim()` as the principal claim name. Operation logging obtains the current username through `CurrentUserProvider`, so JWT and OAuth2 principals follow the same path.

The OAuth2 integration test no longer mocks `JwtDecoder`. It starts a test-only local OIDC discovery/JWKS endpoint, signs JWTs with an RSA key, and lets the production decoder validate signature, issuer, audience, expiry, principal claim, and authorities. Negative cases cover invalid audience, invalid signature, and missing authority. A mutation request verifies that OAuth2 operation logs contain the configured username.

## Permission Matrix

The full API suite keeps administrator happy-path coverage. A parameterized security matrix maps every protected controller method to its expected authority expression and verifies the annotation against seed permissions. Runtime tests use users with narrowly scoped roles to prove that the intended authority succeeds and a neighboring authority is denied. Public endpoints remain separately verified.

## Permission And Role Integrity

Permission updates reject self-parenting and any parent that is a descendant of the current node. Deletion gathers all descendants before deleting role mappings and permission rows in a single transaction.

Role-name validation moves from the controller into `RoleServiceImpl`. Names must start with `ROLE_`, must not exceed the established maximum length, and updates use a repository lookup that excludes the current role id before reporting duplicates.

## Verification

Each production fix starts with a failing focused test. After focused tests pass, initialize `admin_base_it`, run the full API and OAuth2 suites, then run `git diff --check` and `mvn test`. No local MySQL or Redis process is started.
