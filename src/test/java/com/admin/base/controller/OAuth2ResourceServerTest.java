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

    @Test
    void acceptsBearerTokenWithMappedAuthority() throws Exception {
        when(jwtDecoder.decode("oauth-token")).thenReturn(jwtWithAuthorities(List.of("sys:adminList")));

        mockMvc.perform(post("/admin_role/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer oauth-token")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk());
    }

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

    @Test
    void requiresBearerTokenForProtectedApi() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }

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
