package com.admin.base;

import com.admin.base.shared.api.JsonResponse;
import com.admin.base.system.admin.dto.ListAdminParam;
import com.admin.base.auth.dto.CaptchaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 核心功能集成测试，使用 test profile 连接 192.168.3.3 的 MySQL/Redis。
 */
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BaseApplicationTests {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;

    /**
     * 在每个测试执行前基于 WebApplicationContext 重新构建带 springSecurity() 的 MockMvc，
     * 确保安全过滤器链和 Spring 上下文被正确装配。
     */
    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /**
     * 测试 GET /open/captchaImage 公开端点：匿名访问成功并返回 JsonResponse，
     * 响应数据可被反序列化为 CaptchaResponse，证明验证码接口对外契约稳定。
     */
    @Test
    void getCode() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/open/captchaImage")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        JsonResponse response = objectMapper.readValue(content, JsonResponse.class);
        System.out.println(response.toString());
        CaptchaResponse captchaResponse = objectMapper.convertValue(response.getData(), CaptchaResponse.class);
        System.out.println(captchaResponse.toString());
    }

    /**
     * 测试以 sys:adminList 权限登录的 Mock 用户能成功调用 /admin_role/list 接口，
     * 验证带分页参数 ListAdminParam 的请求返回 200 状态码。
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"sys:adminList"})
    void roleList() throws Exception {
        ListAdminParam listAdminParam = new ListAdminParam();
        listAdminParam.setUserName("kyle");
        listAdminParam.setPage(1);
        listAdminParam.setSize(10);
        MvcResult mvcResult = mockMvc.perform(post("/admin_role/list")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(listAdminParam)))
                .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        System.out.println(content);
    }
}