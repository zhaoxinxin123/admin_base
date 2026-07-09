package com.admin.base;

import com.admin.base.common.JsonResponse;
import com.admin.base.dto.request.system.ListAdminParam;
import com.admin.base.dto.request.system.LoginParam;
import com.admin.base.dto.response.system.CaptchaResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
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


@Disabled("Replaced by focused baseline tests during modernization phase 1")
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BaseApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                // 添加spring-security的验证
                .apply(springSecurity())
                .build();
    }

    @Test
    void getCode() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/open/captchaImage")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                )//import com.alibaba.fastjson.JSON;
                //断言:判断状态码  status().isBadRequest()：400错误请求   status().isOk()：正确   status().isNotFound()：验证控制器不存在
                .andExpect(MockMvcResultMatchers.status().isOk())
                // 解析返回的json字段中的属性值是否与断言一样
                .andDo(MockMvcResultHandlers.print()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();//可以拿到返回的内容
        JsonResponse response = JSONObject.parseObject(content, JsonResponse.class);
        System.out.println(response.toString());
        CaptchaResponse captchaResponse = JSONObject.parseObject(response.getData().toString(), CaptchaResponse.class);
        System.out.println(captchaResponse.toString());
    }

    @Test
    void login() throws Exception {
        LoginParam loginRequest = new LoginParam();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("123456");
        loginRequest.setCode("0nat");
        loginRequest.setUuid("bddd2615a1634b258a3cd0f22f91a055");
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/open/login")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON.toJSONString(loginRequest)))//import com.alibaba.fastjson.JSON;
                //断言:判断状态码  status().isBadRequest()：400错误请求   status().isOk()：正确   status().isNotFound()：验证控制器不存在
                .andExpect(MockMvcResultMatchers.status().isOk())
                // 解析返回的json字段中的属性值是否与断言一样
                .andDo(MockMvcResultHandlers.print()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();//可以拿到返回的内容
        System.out.println(content);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"sys:adminList"})
    @WithAnonymousUser
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void roleList() throws Exception {
        ListAdminParam listAdminParam = new ListAdminParam();
        listAdminParam.setUserName("kyle");
        listAdminParam.setPage(1);
        listAdminParam.setSize(10);
        MvcResult mvcResult = mockMvc.perform(post("/admin_role/list")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON.toJSONString(listAdminParam)))
                .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();//可以拿到返回的内容
        System.out.println(content);
    }


}
