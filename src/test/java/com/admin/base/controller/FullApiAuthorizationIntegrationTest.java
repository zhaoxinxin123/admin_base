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

    /**
     * 在每个集成测试执行前重置数据库：清空 7 张系统表后重新执行 v2 种子脚本，
     * 并创建只读权限的 limited 用户，同时建立下载目录，保证后续 API 调用有干净的种子环境。
     */
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

    /**
     * 测试 admin 超级用户可访问系统全部 API（查询、菜单、角色、权限、配置、日志、上传下载），
     * 并能成功完成 admin/role/permission/config/log 的增删改流程；最后验证所有变更型操作
     * 都被异步写入 tb_sys_operation_log 且不会泄露明文密码。
     */
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

    /**
     * 测试只读权限的 limited 用户：可访问公开端点（验证码）、登录、所有已分配查询权限的 API
     * 以及公共上传/下载接口，但所有变更型接口（admin/role/permission/config/log 的增删改）
     * 都会被拦截并返回"没有权限访问"业务错误。
     */
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

    /**
     * 在数据库中创建 limited 用户、ROLE_LIMIT 角色，并把 admin 与该角色绑定；
     * 再为该角色分配 11 个只读相关的权限 id，用于模拟低权限用户的访问场景。
     */
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

    /**
     * 模拟用户登录流程：先获取 /open/captchaImage 中的 uuid 与验证码，
     * 再携带用户名、密码调用 /open/login，返回响应 data.token 字段。
     *
     * @param username 登录用户名
     * @param password 登录密码
     * @return 登录后下发的 JWT token
     */
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

    /**
     * 公共端点回归：使用 token 上传一个 txt 文件，再分别通过 /common/download
     * 和 /common/download/resource2 两种下载方式访问同一资源，覆盖通用上传/下载流程。
     *
     * @param token 当前登录用户的 JWT
     */
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

    /**
     * 公共断言：执行请求并断言 HTTP 200 且业务 code = CODE_OK（200）。
     *
     * @param request MockMvc 请求构造器
     * @return ResultActions 用于在调用方继续追加断言
     */
    private org.springframework.test.web.servlet.ResultActions okJson(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.CODE_OK));
    }

    /**
     * 公共断言：执行请求并断言 HTTP 200 但业务 code = CODE_TOKEN_ERROR（401），
     * msg = "没有权限访问"，用于验证 @PreAuthorize 拒绝受限操作。
     *
     * @param request MockMvc 请求构造器
     */
    private void forbidden(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.CODE_TOKEN_ERROR))
                .andExpect(jsonPath("$.msg").value("没有权限访问"));
    }

    /**
     * 构造一个带 Authorization 头、JSON 请求体的 POST 请求构造器。
     *
     * @param url   请求路径
     * @param token 当前登录用户的 JWT
     * @param body  请求体对象（由 ObjectMapper 序列化为 JSON）
     * @return MockMvc 请求构造器
     */
    private MockHttpServletRequestBuilder postJson(String url, String token, Object body) throws Exception {
        return post(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    /**
     * 构造一个带 Authorization 头、application/x-www-form-urlencoded 请求体的 POST 请求构造器。
     *
     * @param url   请求路径
     * @param token 当前登录用户的 JWT
     * @param pairs 键值对依次排列的表单参数（key1, value1, key2, value2, ...）
     * @return MockMvc 请求构造器
     */
    private MockHttpServletRequestBuilder postForm(String url, String token, String... pairs) {
        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        for (int i = 0; i < pairs.length; i += 2) {
            request.param(pairs[i], pairs[i + 1]);
        }
        return request;
    }

    /**
     * 在 token 前面拼接 "Bearer " 前缀，方便直接写入 Authorization 头。
     */
    private String bearer(String token) {
        return "Bearer " + token;
    }

    /**
     * 生成带前缀且全局唯一的测试名（在 prefix 之后拼接纳秒时间戳），避免不同测试间数据冲突。
     */
    private String unique(String prefix) {
        return prefix + System.nanoTime();
    }

    /**
     * 取纳秒时间戳的第 8~14 位作为唯一数字串，常用于拼接在 perm、role_name 等数据库唯一键后。
     */
    private String uniqueDigits() {
        return Long.toString(System.nanoTime()).substring(8, 14);
    }

    /**
     * 轮询 tb_sys_operation_log 等待指定 operationName 下的所有 expectedUrls 都出现，
     * 用于应对操作日志的异步写入；最多等待 5 秒，超时则断言失败。
     *
     * @param operationName 操作人（与 operation_name 字段对应）
     * @param expectedUrls  期望出现的一组 operation_url
     */
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

    /**
     * 拼接 JdbcTemplate 查询的参数数组：第一个元素是 operationName，
     * 之后依次放入 urls 集合中的每个 url。
     */
    private Object[] parameters(String operationName, Set<String> urls) {
        Object[] parameters = new Object[urls.size() + 1];
        parameters[0] = operationName;
        int index = 1;
        for (String url : urls) {
            parameters[index++] = url;
        }
        return parameters;
    }

    /**
     * 生成 N 个逗号分隔的 ? 占位符，供 IN (...) 之类的 SQL 拼接使用。
     */
    private String placeholders(int size) {
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }

    /**
     * 简易 SQL 文件执行器：按 ; 分批执行 SQL 语句，自动跳过空行和以 -- 开头的注释行，
     * 用于在测试启动时加载 v2 种子脚本。
     *
     * @param path SQL 文件路径
     */
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
