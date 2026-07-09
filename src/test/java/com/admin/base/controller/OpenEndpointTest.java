package com.admin.base.controller;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 公开端点冒烟测试，使用 dev profile 连接 192.168.3.3 的 MySQL/Redis。
 */
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OpenEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void captchaEndpointIsPublicAndCompatible() throws Exception {
        mockMvc.perform(get("/open/captchaImage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists());
    }
}