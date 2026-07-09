package com.admin.base.config.security;

import com.admin.base.BaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全配置测试 — 验证 JWT 安全过滤器链的正确性。
 * 使用 dev profile 连接 192.168.3.3 的 MySQL/Redis。
 */
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 受保护端点拒绝匿名请求，返回 401 状态码。
     */
    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 公开端点 /open/captchaImage 允许匿名访问，返回 200 与标准 JsonResponse 结构。
     */
    @Test
    void openEndpointAllowsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/open/captchaImage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.msg").isString());
    }

    /**
     * 受保护端点返回 401 且响应体为 JSON（非 HTML 默认错误页）。
     */
    @Test
    void protectedEndpointReturnsJsonBody() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.msg").isString());
    }

    /**
     * 无 Authorization 头的受保护端点请求被拒绝。
     */
    @Test
    void protectedEndpointRejectsRequestWithoutAuthHeader() throws Exception {
        mockMvc.perform(get("/admin_role/list"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 无效 token 的受保护请求被拒绝，返回 401。
     */
    @Test
    void protectedEndpointRejectsInvalidToken() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .header("Authorization", "Bearer invalid-token-12345")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }
}