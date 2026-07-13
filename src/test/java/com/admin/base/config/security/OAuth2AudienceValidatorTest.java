package com.admin.base.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AudienceValidatorTest {

    /**
     * 测试 OAuth2AudienceValidator：token 的 aud 包含配置的 audience ("admin-api") 时，
     * 校验通过且不返回错误。
     */
    @Test
    void acceptsTokenWithConfiguredAudience() {
        OAuth2AudienceValidator validator = new OAuth2AudienceValidator(
                new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "authorities")
        );

        assertThat(validator.validate(jwtWithAudience(List.of("admin-api"))).hasErrors()).isFalse();
    }

    /**
     * 测试 OAuth2AudienceValidator：token 的 aud 不包含配置的 audience（仅含 "other-api"）时，
     * 校验失败并返回错误，防止跨服务 token 被本服务误接受。
     */
    @Test
    void rejectsTokenWithoutConfiguredAudience() {
        OAuth2AudienceValidator validator = new OAuth2AudienceValidator(
                new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "authorities")
        );

        assertThat(validator.validate(jwtWithAudience(List.of("other-api"))).hasErrors()).isTrue();
    }

    /**
     * 构造一个带 aud claim 的 OAuth2 Jwt 模拟对象，iss/exp/headers 取占位值，
     * 用于驱动 OAuth2AudienceValidator 的校验逻辑。
     */
    private Jwt jwtWithAudience(List<String> audience) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("aud", audience)
        );
    }
}
