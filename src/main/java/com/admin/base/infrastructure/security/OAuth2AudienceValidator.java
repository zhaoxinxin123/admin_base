package com.admin.base.infrastructure.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class OAuth2AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2Properties properties;

    public OAuth2AudienceValidator(OAuth2Properties properties) {
        this.properties = properties;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (properties.audience() == null || properties.audience().isBlank()) {
            return OAuth2TokenValidatorResult.success();
        }
        if (token.getAudience().contains(properties.audience())) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "The required audience is missing",
                "https://tools.ietf.org/html/rfc6750#section-3.1"
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
