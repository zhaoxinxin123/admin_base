package com.admin.base.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "oauth2")
public class JwtAuthenticationConverterConfig {

    private final AuthorityMapper authorityMapper;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorityMapper::map);
        return converter;
    }
}
