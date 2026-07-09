package com.admin.base;

import com.admin.base.common.JsonResponse;
import com.admin.base.dto.request.system.ListAdminParam;
import com.admin.base.dto.response.system.CaptchaResponse;
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

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

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