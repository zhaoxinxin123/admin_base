package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "admin.auth.mode=oauth2",
        "admin.oauth2.issuer-uri=https://issuer.example"
})
class OAuth2ResourceServerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    /**
     * 测试 OAuth2 Resource Server 模式：携带映射了 sys:adminList 权限的 Bearer token 访问
     * /admin_role/list 时返回 200，验证 OAuth2 解码器和权限映射链路对受保护接口放行。
     */
    @Test
    void acceptsBearerTokenWithMappedAuthority() throws Exception {
        when(jwtDecoder.decode("oauth-token")).thenReturn(jwtWithAuthorities(List.of("sys:adminList")));

        mockMvc.perform(post("/admin_role/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer oauth-token")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk());
    }

    /**
     * 测试 OAuth2 Resource Server 模式下，token 缺少目标接口所需权限时
     * （token 只有 sys:roleList）业务应返回 401 业务码"没有权限访问"，
     * 而非 200，验证 @PreAuthorize 在 OAuth2 链路下生效。
     */
    @Test
    void rejectsBearerTokenWithoutRequiredAuthority() throws Exception {
        when(jwtDecoder.decode("readonly-token")).thenReturn(jwtWithAuthorities(List.of("sys:roleList")));

        mockMvc.perform(post("/admin_role/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer readonly-token")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("没有权限访问"));
    }

    /**
     * 测试 OAuth2 模式下受保护接口在缺少 Authorization 头时返回 401，
     * 验证 Bearer token 是访问受保护 API 的必备凭证。
     */
    @Test
    void requiresBearerTokenForProtectedApi() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 构造一个带指定权限列表的 OAuth2 Jwt 模拟对象，包含 issuer、audience、
     * preferred_username 和 authorities 等 claims，用于驱动 Mockito 桩 JwtDecoder。
     *
     * @param authorities 写入 authorities claim 的权限列表
     * @return 用于测试的 Jwt 实例
     */
    private Jwt jwtWithAuthorities(List<String> authorities) {
        Instant now = Instant.now();
        return new Jwt(
                "token",
                now,
                now.plusSeconds(60),
                java.util.Map.of("alg", "none"),
                java.util.Map.of(
                        "iss", "https://issuer.example",
                        "aud", List.of("admin-api"),
                        "preferred_username", "oauth-admin",
                        "authorities", authorities
                )
        );
    }
}
