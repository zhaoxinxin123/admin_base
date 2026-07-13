package com.admin.base.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AuthorityMapperTest {

    /**
     * 测试 OAuth2AuthorityMapper：当 authorities claim 是字符串列表时，
     * 应当被一一映射为 SimpleGrantedAuthority 列表，权限顺序保持一致。
     */
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

    /**
     * 测试 OAuth2AuthorityMapper：当 authorities claim 是单个字符串（兼容 OAuth2 scope 风格）时，
     * 也能被包装为长度 1 的 SimpleGrantedAuthority 列表，方便对接 OIDC scope。
     */
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

    /**
     * 构造一个带自定义 claims 的 OAuth2 Jwt 模拟对象，iss/exp/headers 取占位值，
     * 用于驱动 OAuth2AuthorityMapper 的不同映射分支。
     */
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
