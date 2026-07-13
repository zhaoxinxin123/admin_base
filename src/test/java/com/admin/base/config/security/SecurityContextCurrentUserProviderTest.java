package com.admin.base.infrastructure.security;

import com.admin.base.shared.exception.BusinessException;
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

    /**
     * 在每个测试执行后清空 SecurityContextHolder，避免上一个测试设置的认证信息污染后续测试。
     */
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 测试本地 JWT 模式下 SecurityContextCurrentUserProvider 解析当前用户：传入
     * UsernamePasswordAuthenticationToken + UserDetailsImpl 时，CurrentUser 应能拿到
     * 用户名、adminId 以及 SimpleGrantedAuthority 列表。
     */
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

    /**
     * 测试 OAuth2 模式下 SecurityContextCurrentUserProvider 解析当前用户：传入
     * JwtAuthenticationToken 时，CurrentUser 应取自 token 的 preferred_username claim、
     * 权限来自 authorities；但因为没有本地 adminId，adminId 应为 null。
     */
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

    /**
     * 测试当 SecurityContext 中没有任何认证信息时，currentUser() 应抛出 BusinessException，
     * 消息为 "请先登录"，防止未登录状态下访问业务代码导致空指针或泄露。
     */
    @Test
    void rejectsMissingAuthentication() {
        assertThatThrownBy(provider::currentUser)
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先登录");
    }
}
