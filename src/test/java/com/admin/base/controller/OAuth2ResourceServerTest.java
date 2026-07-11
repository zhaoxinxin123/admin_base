package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void allowsRequestWithMappedAuthority() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(() -> "sys:adminList"))
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk());
    }
}
