package com.admin.base.infrastructure.security;

import com.admin.base.BaseApplication;
import com.admin.base.support.DevRemoteIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全配置测试 — 验证 JWT 安全过滤器链的正确性。
 * 使用 dev profile 连接远程测试环境的 MySQL/Redis。
 */
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@DevRemoteIntegrationTest
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试受保护端点 /admin_role/list 在没有登录态的 POST 请求下返回 401，
     * 验证 Spring Security 过滤器链在匿名访问时能正确拒绝。
     */
    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试公开端点 /open/captchaImage 允许匿名访问，返回 200 以及标准 JsonResponse
     * 的 code（数字）和 msg（字符串）字段。$.data 的具体内容由 OpenEndpointTest 验证。
     */
    @Test
    void openEndpointAllowsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/open/captchaImage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.msg").isString());
    }

    /**
     * 测试受保护端点被拒绝时返回的是 JSON 错误体（code 为数字、msg 为字符串），
     * 而非 Spring Security 默认的 HTML 错误页，保证前端可统一解析。
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
     * 测试对受保护端点的 GET 请求在缺少 Authorization 头时被拒绝，返回 401，
     * 验证无认证信息的所有 HTTP 方法都被拦截。
     */
    @Test
    void protectedEndpointRejectsRequestWithoutAuthHeader() throws Exception {
        mockMvc.perform(get("/admin_role/list"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试受保护端点收到伪造的 Bearer token 时返回 401，并附带 JsonResponse 错误体，
     * 验证 MyTokenFilter 能正确拦截非法 token 而非让其穿透到业务层。
     */
    @Test
    void protectedEndpointRejectsInvalidToken() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .header("Authorization", "Bearer invalid-token-12345")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.msg").isString());
    }
}
