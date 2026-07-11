package com.admin.base.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AudienceValidatorTest {

    @Test
    void acceptsTokenWithConfiguredAudience() {
        OAuth2AudienceValidator validator = new OAuth2AudienceValidator(
                new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "authorities")
        );

        assertThat(validator.validate(jwtWithAudience(List.of("admin-api"))).hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenWithoutConfiguredAudience() {
        OAuth2AudienceValidator validator = new OAuth2AudienceValidator(
                new OAuth2Properties("https://issuer.example", "admin-api", "preferred_username", "authorities")
        );

        assertThat(validator.validate(jwtWithAudience(List.of("other-api"))).hasErrors()).isTrue();
    }

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
