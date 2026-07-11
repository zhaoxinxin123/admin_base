package com.admin.base.config.security;

import com.admin.base.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityContextCurrentUserProviderTest {

    private final CurrentUserProvider provider = new SecurityContextCurrentUserProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsLocalJwtUserWithAdminId() {
        UserDetailsImpl details = new UserDetailsImpl();
        details.setUsername("admin");
        details.setAdminId(1L);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("sys:adminList"))
        );
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CurrentUser currentUser = provider.currentUser();

        assertThat(currentUser.username()).isEqualTo("admin");
        assertThat(currentUser.adminId()).isEqualTo(1L);
        assertThat(currentUser.authorities()).containsExactly("sys:adminList");
    }

    @Test
    void returnsOauth2UserWithoutLocalAdminId() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("preferred_username", "oidc-admin")
        );
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("sys:adminList")),
                "oidc-admin"
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CurrentUser currentUser = provider.currentUser();

        assertThat(currentUser.username()).isEqualTo("oidc-admin");
        assertThat(currentUser.adminId()).isNull();
        assertThat(currentUser.authorities()).containsExactly("sys:adminList");
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThatThrownBy(provider::currentUser)
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先登录");
    }
}
