package com.admin.base.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterConfigTest {

    @Test
    void usesConfiguredUsernameClaimAsAuthenticationName() {
        AuthorityMapper mapper = jwt -> List.of(new SimpleGrantedAuthority("sys:adminList"));
        OAuth2Properties properties = new OAuth2Properties(
                "https://issuer.example", "admin-api", "account_name", "authorities");
        JwtAuthenticationConverterConfig config = new JwtAuthenticationConverterConfig(mapper, properties);
        Instant now = Instant.now();
        Jwt jwt = new Jwt(
                "token",
                now,
                now.plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", "opaque-id", "account_name", "oauth-admin")
        );

        AbstractAuthenticationToken authentication = config.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("oauth-admin");
        assertThat(authentication.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("sys:adminList");
    }
}
