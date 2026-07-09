package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全边界测试，使用 dev profile 连接 192.168.3.3 的 MySQL/Redis。
 */
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityBoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }
}