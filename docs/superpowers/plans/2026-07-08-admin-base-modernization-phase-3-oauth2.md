# Admin Base Modernization Phase 3 OAuth2 OIDC Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the reserved OAuth2/OIDC authentication mode so `admin_base` can run either in local JWT mode or as an OAuth2 Resource Server integrated with an external identity provider.

**Architecture:** Phase 3 keeps JWT mode as the default and adds a separate OAuth2 Resource Server security chain activated by `admin.auth.mode=oauth2`. The application does not become an Authorization Server. It validates external bearer tokens, maps token claims to the existing role/permission model, and keeps API authorization based on Spring Security authorities.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Security OAuth2 Resource Server, Spring Security OAuth2 JOSE, JWT claims mapping, MySQL/JPA persistence from Phase 2, Redis retained for local JWT mode.

## Global Constraints

- Continue using MySQL.
- Do not create database foreign keys.
- Keep existing response exterior `{code,msg,data}`.
- Keep JWT mode available and default.
- OAuth2/OIDC mode is selected at startup using `admin.auth.mode=oauth2`.
- The project does not issue OAuth2 tokens in this phase.
- The project integrates with an external OIDC provider such as Keycloak, Casdoor, enterprise OIDC, Auth0, or Okta.
- Do not remove local JWT login endpoints unless a separate compatibility decision is made.
- Do not reintroduce MyBatis Plus or removed special-purpose tools.
- Do not include unrelated existing worktree changes in commits.

---

## File Structure Map

- `pom.xml`: add OAuth2 Resource Server and JOSE dependencies.
- `src/main/resources/application.yml`: add OAuth2 issuer and claim mapping properties.
- `src/main/java/com/admin/base/config/security/AuthModeProperties.java`: already created in Phase 1; may be extended.
- `src/main/java/com/admin/base/config/security/JwtSecurityConfig.java`: local JWT security chain.
- `src/main/java/com/admin/base/config/security/OAuth2ResourceServerSecurityConfig.java`: external token security chain.
- `src/main/java/com/admin/base/config/security/AuthorityMapper.java`: common authority mapping contract.
- `src/main/java/com/admin/base/config/security/LocalAuthorityMapper.java`: maps local DB roles/permissions for JWT mode.
- `src/main/java/com/admin/base/config/security/OAuth2AuthorityMapper.java`: maps external claims to authorities.
- `src/main/java/com/admin/base/config/security/CurrentUserProvider.java`: common current-user abstraction.
- `src/test/java/com/admin/base/config/security/*.java`: mode and mapping tests.

## Task 1: Add OAuth2 Dependencies and Properties

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/com/admin/base/config/security/AuthModeProperties.java`
- Create: `src/main/java/com/admin/base/config/security/OAuth2Properties.java`
- Create: `src/test/java/com/admin/base/config/security/OAuth2PropertiesTest.java`

**Interfaces:**
- Produces: `OAuth2Properties` with issuer URI, audience, authorities claim, username claim.

- [ ] **Step 1: Add dependencies**

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

- [ ] **Step 2: Add OAuth2 configuration defaults**

In `src/main/resources/application.yml`:

```yaml
admin:
  auth:
    mode: ${ADMIN_AUTH_MODE:jwt}
  oauth2:
    issuer-uri: ${ADMIN_OAUTH2_ISSUER_URI:}
    audience: ${ADMIN_OAUTH2_AUDIENCE:admin-api}
    username-claim: ${ADMIN_OAUTH2_USERNAME_CLAIM:preferred_username}
    authorities-claim: ${ADMIN_OAUTH2_AUTHORITIES_CLAIM:authorities}
```

- [ ] **Step 3: Create properties record**

Create `src/main/java/com/admin/base/config/security/OAuth2Properties.java`:

```java
package com.admin.base.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.oauth2")
public record OAuth2Properties(
        String issuerUri,
        String audience,
        String usernameClaim,
        String authoritiesClaim
) {
    public OAuth2Properties {
        if (audience == null || audience.isBlank()) {
            audience = "admin-api";
        }
        if (usernameClaim == null || usernameClaim.isBlank()) {
            usernameClaim = "preferred_username";
        }
        if (authoritiesClaim == null || authoritiesClaim.isBlank()) {
            authoritiesClaim = "authorities";
        }
    }
}
```

- [ ] **Step 4: Register properties**

In the security configuration package, register both property classes:

```java
@EnableConfigurationProperties({AuthModeProperties.class, OAuth2Properties.class})
```

- [ ] **Step 5: Add properties test**

Create `OAuth2PropertiesTest`:

```java
package com.admin.base.config.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2PropertiesTest {

    @Test
    void defaultsClaimNames() {
        OAuth2Properties properties = new OAuth2Properties("", null, null, null);

        assertThat(properties.audience()).isEqualTo("admin-api");
        assertThat(properties.usernameClaim()).isEqualTo("preferred_username");
        assertThat(properties.authoritiesClaim()).isEqualTo("authorities");
    }
}
```

- [ ] **Step 6: Run tests**

Run:

```bash
mvn test -Dtest=OAuth2PropertiesTest
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add pom.xml src/main/resources/application.yml src/main/java/com/admin/base/config/security src/test/java/com/admin/base/config/security/OAuth2PropertiesTest.java
git commit -m "feat: add oauth2 resource server configuration"
```

## Task 2: Split Security Chains by Auth Mode

**Files:**
- Modify: `src/main/java/com/admin/base/config/security/SecurityConfig.java`
- Create: `src/main/java/com/admin/base/config/security/JwtSecurityConfig.java`
- Create: `src/main/java/com/admin/base/config/security/OAuth2ResourceServerSecurityConfig.java`
- Test: `src/test/java/com/admin/base/config/security/AuthModeSecurityConfigTest.java`

**Interfaces:**
- Consumes: `AuthModeProperties`.
- Produces: one active `SecurityFilterChain` per auth mode.

- [ ] **Step 1: Keep shared beans in `SecurityConfig`**

Move shared beans only into `SecurityConfig`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({AuthModeProperties.class, OAuth2Properties.class})
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: Create JWT security config**

Create `JwtSecurityConfig`:

```java
package com.admin.base.config.security;

import com.admin.base.filter.MyTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "jwt", matchIfMissing = true)
public class JwtSecurityConfig {

    private final InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint;
    private final AuthenticationProvider authenticationProvider;
    private final MyTokenFilter myTokenFilter;

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(invalidAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/open/**", "/error").permitAll()
                        .requestMatchers("/actuator/health", "/prometheus").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(myTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- [ ] **Step 3: Create OAuth2 Resource Server config**

Create `OAuth2ResourceServerSecurityConfig`:

```java
package com.admin.base.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "oauth2")
public class OAuth2ResourceServerSecurityConfig {

    private final InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(invalidAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/open/**", "/error").permitAll()
                        .requestMatchers("/actuator/health", "/prometheus").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }
}
```

- [ ] **Step 4: Add mode config tests**

Create `AuthModeSecurityConfigTest` with `ApplicationContextRunner`:

```java
package com.admin.base.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AuthModeSecurityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SecurityConfig.class, JwtSecurityConfig.class, OAuth2ResourceServerSecurityConfig.class);

    @Test
    void jwtModeLoadsJwtConfig() {
        contextRunner.withPropertyValues("admin.auth.mode=jwt")
                .run(context -> assertThat(context).hasSingleBean(JwtSecurityConfig.class));
    }

    @Test
    void oauth2ModeLoadsOauth2Config() {
        contextRunner.withPropertyValues("admin.auth.mode=oauth2")
                .run(context -> assertThat(context).hasSingleBean(OAuth2ResourceServerSecurityConfig.class));
    }
}
```

- [ ] **Step 5: Run tests**

Run:

```bash
mvn test -Dtest=AuthModeSecurityConfigTest
```

Expected: PASS after adding required test stubs or imports.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/admin/base/config/security src/test/java/com/admin/base/config/security/AuthModeSecurityConfigTest.java
git commit -m "feat: split security chains by auth mode"
```

## Task 3: Implement OAuth2 Authority Mapping

**Files:**
- Create: `src/main/java/com/admin/base/config/security/AuthorityMapper.java`
- Create: `src/main/java/com/admin/base/config/security/OAuth2AuthorityMapper.java`
- Create: `src/main/java/com/admin/base/config/security/JwtAuthenticationConverterConfig.java`
- Test: `src/test/java/com/admin/base/config/security/OAuth2AuthorityMapperTest.java`

**Interfaces:**
- Produces: `Collection<GrantedAuthority> map(Jwt jwt)`.

- [ ] **Step 1: Create mapper contract**

```java
package com.admin.base.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public interface AuthorityMapper {
    Collection<GrantedAuthority> map(Jwt jwt);
}
```

- [ ] **Step 2: Implement OAuth2 mapper**

```java
package com.admin.base.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2AuthorityMapper implements AuthorityMapper {

    private final OAuth2Properties properties;

    @Override
    public Collection<GrantedAuthority> map(Jwt jwt) {
        Object claim = jwt.getClaims().get(properties.authoritiesClaim());
        if (claim instanceof Collection<?> values) {
            return values.stream()
                    .map(String::valueOf)
                    .filter(value -> !value.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
        }
        if (claim instanceof String value && !value.isBlank()) {
            return List.of(new SimpleGrantedAuthority(value));
        }
        return List.of();
    }
}
```

- [ ] **Step 3: Add converter config**

```java
package com.admin.base.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationConverterConfig {

    private final AuthorityMapper authorityMapper;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorityMapper::map);
        return converter;
    }
}
```

- [ ] **Step 4: Add mapper test**

```java
package com.admin.base.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AuthorityMapperTest {

    @Test
    void mapsAuthoritiesClaimToGrantedAuthorities() {
        OAuth2Properties properties = new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "authorities");
        OAuth2AuthorityMapper mapper = new OAuth2AuthorityMapper(properties);
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("authorities", List.of("sys:adminList", "sys:roleList"))
        );

        assertThat(mapper.map(jwt))
                .extracting("authority")
                .containsExactly("sys:adminList", "sys:roleList");
    }
}
```

- [ ] **Step 5: Run tests**

Run:

```bash
mvn test -Dtest=OAuth2AuthorityMapperTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/admin/base/config/security src/test/java/com/admin/base/config/security/OAuth2AuthorityMapperTest.java
git commit -m "feat: map oauth2 claims to authorities"
```

## Task 4: Implement Current User Abstraction

**Files:**
- Create: `src/main/java/com/admin/base/config/security/CurrentUser.java`
- Create: `src/main/java/com/admin/base/config/security/CurrentUserProvider.java`
- Create: `src/main/java/com/admin/base/config/security/SecurityContextCurrentUserProvider.java`
- Modify: `src/main/java/com/admin/base/controller/common/BaseController.java`
- Test: `src/test/java/com/admin/base/config/security/SecurityContextCurrentUserProviderTest.java`

**Interfaces:**
- Produces: `CurrentUserProvider.currentUser(): CurrentUser`.

- [ ] **Step 1: Create current user record**

```java
package com.admin.base.config.security;

import java.util.List;

public record CurrentUser(String username, Long adminId, List<String> authorities) {
}
```

- [ ] **Step 2: Create provider interface**

```java
package com.admin.base.config.security;

public interface CurrentUserProvider {
    CurrentUser currentUser();
}
```

- [ ] **Step 3: Implement provider**

```java
package com.admin.base.config.security;

import com.admin.base.constant.ResponseCode;
import com.admin.base.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResponseCode.CODE_NO_LOGIN, "请先登录");
        }
        String username = authentication.getName();
        Long adminId = authentication.getDetails() instanceof UserDetailsImpl userDetails
                ? userDetails.getAdminId().longValue()
                : null;
        var authorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList();
        return new CurrentUser(username, adminId, authorities);
    }
}
```

- [ ] **Step 4: Update BaseController**

Inject `CurrentUserProvider` and replace direct `SecurityContextHolder` usage:

```java
protected String getUserName() {
    return currentUserProvider.currentUser().username();
}
```

- [ ] **Step 5: Run tests**

Run:

```bash
mvn test -Dtest=SecurityContextCurrentUserProviderTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/admin/base/config/security src/main/java/com/admin/base/controller/common/BaseController.java src/test/java/com/admin/base/config/security/SecurityContextCurrentUserProviderTest.java
git commit -m "refactor: abstract current user lookup"
```

## Task 5: Add OAuth2 Mode Integration Tests

**Files:**
- Create: `src/test/java/com/admin/base/controller/OAuth2ResourceServerTest.java`

**Interfaces:**
- Produces: MockMvc tests proving OAuth2 bearer token authorization works with mapped authorities.

- [ ] **Step 1: Create OAuth2 MockMvc test**

```java
package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "admin.auth.mode=oauth2",
        "admin.oauth2.issuer-uri=https://issuer.example"
})
class OAuth2ResourceServerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsRequestWithMappedAuthority() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(() -> "sys:adminList"))
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run OAuth2 test**

Run:

```bash
mvn test -Dtest=OAuth2ResourceServerTest
```

Expected: PASS if the endpoint's service dependencies are test-configurable. If persistence dependencies block the web test, replace the controller dependencies with `@MockBean` in this test class and run again.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/admin/base/controller/OAuth2ResourceServerTest.java
git commit -m "test: cover oauth2 resource server mode"
```

## Task 6: Document Provider Setup

**Files:**
- Create: `docs/modernization/oauth2-provider-setup.md`
- Modify: `src/main/resources/application-dev.yml`
- Modify: `src/main/resources/application-prod.yml`

**Interfaces:**
- Produces: operator-facing setup docs for external OIDC providers.

- [ ] **Step 1: Add provider setup doc**

Create `docs/modernization/oauth2-provider-setup.md`:

```markdown
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
- Token should contain a username claim.
- Token should contain an authorities claim with permission strings such as `sys:adminList`.

## Responsibility Split

- External provider handles login, SSO session, password policy, MFA, and token issuance.
- `admin_base` validates bearer tokens and enforces API permissions.
- Local `/open/login` remains for JWT mode.
```

- [ ] **Step 2: Add env placeholders to profile files**

In `application-dev.yml` and `application-prod.yml`, add:

```yaml
admin:
  oauth2:
    issuer-uri: ${ADMIN_OAUTH2_ISSUER_URI:}
    audience: ${ADMIN_OAUTH2_AUDIENCE:admin-api}
    username-claim: ${ADMIN_OAUTH2_USERNAME_CLAIM:preferred_username}
    authorities-claim: ${ADMIN_OAUTH2_AUTHORITIES_CLAIM:authorities}
```

- [ ] **Step 3: Commit**

```bash
git add docs/modernization/oauth2-provider-setup.md src/main/resources/application-dev.yml src/main/resources/application-prod.yml
git commit -m "docs: add oauth2 provider setup guide"
```

## Task 7: Final Phase 3 Verification

**Files:**
- Create: `docs/modernization/phase-3-verification.md`

**Interfaces:**
- Produces: evidence checklist for OAuth2/OIDC mode.

- [ ] **Step 1: Create verification checklist**

Create `docs/modernization/phase-3-verification.md`:

```markdown
# Phase 3 Verification

## Required Commands

- `mvn test`
- `mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest`
- `rg -n "admin.auth.mode|admin.oauth2" src/main/resources docs/modernization`

## Expected Results

- JWT mode remains default.
- OAuth2 mode loads the Resource Server security chain.
- OAuth2 JWT authorities map to existing permission strings.
- Provider setup documentation exists.
- The project does not issue OAuth2 tokens.
```

- [ ] **Step 2: Run verification commands**

Run:

```bash
mvn test
mvn test -Dtest=OAuth2AuthorityMapperTest,OAuth2PropertiesTest,AuthModeSecurityConfigTest,OAuth2ResourceServerTest
rg -n "admin.auth.mode|admin.oauth2" src/main/resources docs/modernization
```

Expected: test commands exit 0 and scan shows configuration plus documentation.

- [ ] **Step 3: Commit**

```bash
git add docs/modernization/phase-3-verification.md
git commit -m "docs: add phase 3 verification checklist"
```

## Self-Review Checklist

- Spec coverage: Covers startup auth-mode switching, external OIDC provider integration, Resource Server JWT validation, authority claim mapping, current-user abstraction, and documentation.
- Boundary: Does not implement an Authorization Server and does not remove JWT mode.
- Type consistency: `OAuth2Properties`, `AuthorityMapper`, and `CurrentUserProvider` are introduced before dependent config and tests.
- Planning marker scan: checked for banned markers and found none.

