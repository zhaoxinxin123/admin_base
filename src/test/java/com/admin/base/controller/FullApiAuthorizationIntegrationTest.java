package com.admin.base.controller;

import com.admin.base.BaseApplication;
import com.admin.base.shared.constant.DownloadType;
import com.admin.base.shared.constant.ResponseCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "sysconfig.upload-path=${java.io.tmpdir}/admin-base-test/upload",
        "sysconfig.download-path=${java.io.tmpdir}/admin-base-test/download",
        "sysconfig.local-store=${java.io.tmpdir}/admin-base-test/local"
})
class FullApiAuthorizationIntegrationTest {

    private static final String SEED_PASSWORD_HASH = "$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi";
    private static final Set<String> MUTATION_LOG_URLS = Set.of(
            "/admin/add",
            "/admin/delete",
            "/admin_role/resetPassword",
            "/admin_role/updateState",
            "/admin_role/updateAdminOfRole",
            "/role/add",
            "/role/update",
            "/role/delete",
            "/permissions/add",
            "/permissions/update",
            "/permissions/delete",
            "/sys_global_config/add",
            "/sys_global_config/update",
            "/sys_global_config/deleteBatch",
            "/sys_operation_log/deleteBatch"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${sysconfig.download-path}")
    private Path downloadPath;

    @BeforeEach
    void resetDatabase() throws Exception {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        for (String table : List.of(
                "tb_sys_operation_log",
                "tb_sys_role_permission",
                "tb_sys_admin_role",
                "tb_sys_global_config",
                "tb_sys_permissions",
                "tb_sys_role",
                "tb_sys_admin")) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        executeSqlFile(Path.of("docs/database/admin-base-seed-v2.sql"));
        createLimitedUser();
        Files.createDirectories(downloadPath);
    }

    @Test
    void adminCanExerciseAllSystemApisAndMutationLogsAreComplete() throws Exception {
        String token = login("admin", "123456");

        okJson(postJson("/admin_role/list", token, Map.of("page", 1, "size", 10)));
        okJson(postForm("/admin/getMenu", token))
                .andExpect(jsonPath("$.data.length()").value(1));
        okJson(postForm("/role/all", token));
        okJson(postForm("/role_permission/manageList", token, "page", "1", "size", "10"))
                .andExpect(jsonPath("$.data.rows[0].permissionResponses.length()").value(1));
        okJson(postForm("/permissions/list", token))
                .andExpect(jsonPath("$.data.length()").value(1));
        okJson(postForm("/sys_global_config/list", token, "page", "1", "size", "10"));
        okJson(postForm("/sys_operation_log/list", token, "page", "1", "size", "10"));
        exerciseCommonEndpoints(token);

        String adminName = unique("api_admin");
        okJson(postForm("/admin/add", token,
                "account", adminName,
                "password", "Abc123",
                "nickName", "API Admin",
                "roleIds", "1"));
        Integer adminId = jdbcTemplate.queryForObject(
                "select admin_id from tb_sys_admin where user_name = ?",
                Integer.class,
                adminName);
        okJson(postForm("/admin_role/resetPassword", token, "adminId", adminId.toString(), "password", "Def456"));
        okJson(postForm("/admin_role/updateState", token, "adminId", adminId.toString(), "state", "0"));
        okJson(postForm("/admin_role/updateAdminOfRole", token, "adminId", adminId.toString(), "roleIds", "1"));
        okJson(postForm("/admin/delete", token, "adminId", adminId.toString()));

        String roleName = "ROLE_" + uniqueDigits();
        okJson(postForm("/role/add", token, "roleName", roleName, "note", "temporary role", "permissionIds", "1"));
        Integer roleId = jdbcTemplate.queryForObject(
                "select role_id from tb_sys_role where role_name = ?",
                Integer.class,
                roleName);
        okJson(postForm("/role/update", token, "roleId", roleId.toString(), "roleName", roleName, "note", "updated role", "permissionIds", "1"));
        okJson(postForm("/role/delete", token, "roleId", roleId.toString()));

        String permissionPerm = "sys:test:" + uniqueDigits();
        okJson(postForm("/permissions/add", token,
                "parentId", "1",
                "perm", permissionPerm,
                "icon", "test",
                "name", "test",
                "state", "1",
                "title", "Test Permission",
                "path", "/system/test"));
        Integer permissionId = jdbcTemplate.queryForObject(
                "select permission_id from tb_sys_permissions where perm = ?",
                Integer.class,
                permissionPerm);
        okJson(postForm("/permissions/update", token,
                "permissionId", permissionId.toString(),
                "parentId", "1",
                "perm", permissionPerm,
                "icon", "test",
                "name", "test",
                "state", "1",
                "title", "Updated Permission",
                "path", "/system/test-updated"));
        okJson(postForm("/permissions/delete", token, "permissionId", permissionId.toString()));

        String configKey = "api.test." + uniqueDigits();
        okJson(postForm("/sys_global_config/add", token, "key", configKey, "value", "one", "note", "temporary config"));
        Integer configId = jdbcTemplate.queryForObject(
                "select config_id from tb_sys_global_config where config_key = ?",
                Integer.class,
                configKey);
        okJson(postForm("/sys_global_config/update", token, "configId", configId.toString(), "key", configKey, "value", "two", "note", "updated config"));
        okJson(postForm("/sys_global_config/deleteBatch", token, "configIds", configId.toString()));

        jdbcTemplate.update("""
                insert into tb_sys_operation_log
                  (title, business_type, method, request_method, operation_name, operation_url, operation_ip, success, status_code, operation_time)
                values ('seed', 0, 'seed()', 'POST', 'seed', '/seed', '127.0.0.1', 1, 200, now())
                """);
        Integer logId = jdbcTemplate.queryForObject("select max(operation_id) from tb_sys_operation_log where operation_url = '/seed'", Integer.class);
        okJson(postForm("/sys_operation_log/deleteBatch", token, "logIds", logId.toString()));

        waitForMutationLogs("admin", MUTATION_LOG_URLS);
        Integer leakedPasswords = jdbcTemplate.queryForObject("""
                select count(*) from tb_sys_operation_log
                where operation_param like '%Abc123%'
                   or operation_param like '%Def456%'
                   or operation_param like '%123456%'
                """, Integer.class);
        assertThat(leakedPasswords).isZero();
    }

    @Test
    void limitedUserCanOnlyAccessPublicAuthenticatedAndGrantedPermissionApis() throws Exception {
        okJson(get("/open/captchaImage"));
        mockMvc.perform(post("/admin_role/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());

        String token = login("limited", "123456");
        okJson(postJson("/admin_role/list", token, Map.of("page", 1, "size", 10)));
        okJson(postForm("/admin/getMenu", token));
        okJson(postForm("/role/all", token));
        okJson(postForm("/role_permission/manageList", token, "page", "1", "size", "10"));
        okJson(postForm("/permissions/list", token));
        okJson(postForm("/sys_global_config/list", token, "page", "1", "size", "10"));
        okJson(postForm("/sys_operation_log/list", token, "page", "1", "size", "10"));
        exerciseCommonEndpoints(token);

        forbidden(postForm("/admin/add", token, "account", "blocked", "password", "Abc123", "nickName", "Blocked", "roleIds", "1"));
        forbidden(postForm("/admin/delete", token, "adminId", "1"));
        forbidden(postForm("/admin_role/resetPassword", token, "adminId", "1", "password", "Def456"));
        forbidden(postForm("/admin_role/updateState", token, "adminId", "1", "state", "0"));
        forbidden(postForm("/admin_role/updateAdminOfRole", token, "adminId", "1", "roleIds", "1"));
        forbidden(postForm("/role/add", token, "roleName", "ROLE_Z9999", "note", "blocked", "permissionIds", "1"));
        forbidden(postForm("/role/update", token, "roleId", "1", "roleName", "ROLE_ADMIN", "note", "blocked", "permissionIds", "1"));
        forbidden(postForm("/role/delete", token, "roleId", "1"));
        forbidden(postForm("/permissions/add", token, "parentId", "1", "perm", "sys:block", "state", "1", "title", "Blocked", "path", "/blocked"));
        forbidden(postForm("/permissions/update", token, "permissionId", "1", "parentId", "0", "perm", "sys:blocked", "state", "1", "title", "Blocked", "path", "/blocked"));
        forbidden(postForm("/permissions/delete", token, "permissionId", "1"));
        forbidden(postForm("/sys_global_config/add", token, "key", "blocked", "value", "blocked", "note", "blocked"));
        forbidden(postForm("/sys_global_config/update", token, "configId", "1", "key", "blocked", "value", "blocked", "note", "blocked"));
        forbidden(postForm("/sys_global_config/deleteBatch", token, "configIds", "1"));
        forbidden(postForm("/sys_operation_log/deleteBatch", token, "logIds", "1"));
    }

    private void createLimitedUser() {
        jdbcTemplate.update("""
                insert into tb_sys_admin (admin_id, nickname, user_name, password, state, create_time, update_time)
                values (2, 'Limited User', 'limited', ?, 0, now(), now())
                """, SEED_PASSWORD_HASH);
        jdbcTemplate.update("""
                insert into tb_sys_role (role_id, role_name, note, create_time, update_time)
                values (2, 'ROLE_LIMIT', 'Query-only test role', now(), now())
                """);
        jdbcTemplate.update("insert into tb_sys_admin_role (admin_id, role_id, create_time) values (2, 2, now())");
        for (int permissionId : List.of(1, 2, 3, 4, 5, 6, 7, 12, 17, 21, 25)) {
            jdbcTemplate.update("insert into tb_sys_role_permission (role_id, permission_id, create_time) values (2, ?, now())", permissionId);
        }
    }

    private String login(String username, String password) throws Exception {
        JsonNode captcha = objectMapper.readTree(okJson(get("/open/captchaImage")).andReturn().getResponse().getContentAsString());
        String uuid = captcha.at("/data/uuid").asText();
        String code = captcha.at("/data/tmpVar").asText();
        MockHttpServletRequestBuilder loginRequest = post("/open/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password,
                        "uuid", uuid,
                        "code", code)));
        JsonNode loginResponse = objectMapper.readTree(okJson(loginRequest).andReturn().getResponse().getContentAsString());
        return loginResponse.at("/data/token").asText();
    }

    private void exerciseCommonEndpoints(String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes(StandardCharsets.UTF_8));
        okJson(multipart("/common/upload").file(file).header("Authorization", bearer(token)));

        Files.writeString(downloadPath.resolve("sample_hello.txt"), "hello", StandardCharsets.UTF_8);
        mockMvc.perform(get("/common/download")
                        .header("Authorization", bearer(token))
                        .param("fileName", "sample_hello.txt")
                        .param("type", String.valueOf(DownloadType.DOWNLOAD))
                        .param("delete", "false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/common/download/resource2")
                        .header("Authorization", bearer(token))
                        .param("resource", "sample_hello.txt"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions okJson(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.CODE_OK));
    }

    private void forbidden(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.CODE_TOKEN_ERROR))
                .andExpect(jsonPath("$.msg").value("没有权限访问"));
    }

    private MockHttpServletRequestBuilder postJson(String url, String token, Object body) throws Exception {
        return post(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private MockHttpServletRequestBuilder postForm(String url, String token, String... pairs) {
        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        for (int i = 0; i < pairs.length; i += 2) {
            request.param(pairs[i], pairs[i + 1]);
        }
        return request;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String unique(String prefix) {
        return prefix + System.nanoTime();
    }

    private String uniqueDigits() {
        return Long.toString(System.nanoTime()).substring(8, 14);
    }

    private void waitForMutationLogs(String operationName, Set<String> expectedUrls) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        Set<String> seen = new LinkedHashSet<>();
        while (System.nanoTime() < deadline) {
            seen.clear();
            seen.addAll(jdbcTemplate.queryForList("""
                    select distinct operation_url
                    from tb_sys_operation_log
                    where operation_name = ?
                      and operation_url in (%s)
                    """.formatted(placeholders(expectedUrls.size())), String.class, parameters(operationName, expectedUrls)));
            if (seen.containsAll(expectedUrls)) {
                return;
            }
            Thread.sleep(100);
        }
        assertThat(seen).containsAll(expectedUrls);
    }

    private Object[] parameters(String operationName, Set<String> urls) {
        Object[] parameters = new Object[urls.size() + 1];
        parameters[0] = operationName;
        int index = 1;
        for (String url : urls) {
            parameters[index++] = url;
        }
        return parameters;
    }

    private String placeholders(int size) {
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }

    private void executeSqlFile(Path path) throws Exception {
        StringBuilder statement = new StringBuilder();
        for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = rawLine.strip();
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            statement.append(rawLine).append('\n');
            if (line.endsWith(";")) {
                jdbcTemplate.execute(statement.toString());
                statement.setLength(0);
            }
        }
        if (!statement.isEmpty()) {
            jdbcTemplate.execute(statement.toString());
        }
    }
}
