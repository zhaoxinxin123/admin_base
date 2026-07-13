package com.admin.base.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public interface AuthorityMapper {
    Collection<GrantedAuthority> map(Jwt jwt);
}
