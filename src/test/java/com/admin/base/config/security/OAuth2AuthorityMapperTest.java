package com.admin.base.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AuthorityMapperTest {

    @Test
    void mapsAuthoritiesClaimToGrantedAuthorities() {
        OAuth2AuthorityMapper mapper = new OAuth2AuthorityMapper(
                new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "authorities")
        );
        Jwt jwt = jwtWithClaims(Map.of("authorities", List.of("sys:adminList", "sys:roleList")));

        assertThat(mapper.map(jwt))
                .extracting("authority")
                .containsExactly("sys:adminList", "sys:roleList");
    }

    @Test
    void mapsStringAuthorityClaimToSingleGrantedAuthority() {
        OAuth2AuthorityMapper mapper = new OAuth2AuthorityMapper(
                new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "scope")
        );
        Jwt jwt = jwtWithClaims(Map.of("scope", "sys:adminList"));

        assertThat(mapper.map(jwt))
                .extracting("authority")
                .containsExactly("sys:adminList");
    }

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                claims
        );
    }
}
