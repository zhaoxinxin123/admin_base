package com.admin.base.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "oauth2")
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
