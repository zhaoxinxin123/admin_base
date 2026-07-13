package com.admin.base.infrastructure.security;

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
