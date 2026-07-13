package com.admin.base;

import com.admin.base.system.role.entity.RolePermission;
import com.admin.base.system.admin.repository.AdminRepository;
import com.admin.base.system.config.repository.GlobalConfigRepository;
import com.admin.base.system.permission.repository.PermissionsRepository;
import com.admin.base.system.role.repository.RolePermissionRepository;
import com.admin.base.system.role.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 基于 docs/database/admin-base-seed-v2.sql 的逻辑覆盖测试。
 *
 * <p>测试使用 test profile 连接 192.168.3.3 的 MySQL/Redis，不启动本地依赖。</p>
 */
@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SeedV2LogicCoverageTest {

    private static final List<Long> SEED_PERMISSION_IDS = LongStream.rangeClosed(1, 26)
            .boxed()
            .toList();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PermissionsRepository permissionsRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    /**
     * 在每个测试方法执行前初始化 v2 种子数据 fixture，包括管理员、角色、权限、角色权限关联以及全局配置。
     * 目的是保证测试在一个干净且可重复的数据库状态下运行。
     */
    @BeforeEach
    void installSeedV2Fixture() {
        seedAdmin();
        seedRole();
        seedPermissions();
        seedAdminRole();
        seedRolePermissions();
        seedGlobalConfig();
    }

    /**
     * 测试 v2 种子脚本中的核心数据是否正确入库：管理员账号、ROLE_ADMIN 角色、26 个种子权限
     * 全部存在并且 ROLE_ADMIN 拥有全部种子权限 ID，同时验证全局配置中下载/上传路径和系统版本号的值。
     */
    @Test
    void seedV2CoreDataExistsAndRoleAdminOwnsAllSeedPermissions() {
        var admin = adminRepository.findByUserName("admin").orElseThrow();
        assertThat(admin.getAdminId()).isEqualTo(1L);
        assertThat(admin.getNickname()).isEqualTo("管理员");
        assertThat(admin.getState()).isZero();

        var role = roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
        assertThat(role.getRoleId()).isEqualTo(1L);
        assertThat(role.getNote()).isEqualTo("管理员");

        assertThat(permissionsRepository.findAllById(SEED_PERMISSION_IDS))
                .extracting("perm")
                .contains(
                        "sys:manage",
                        "sys:adminList",
                        "sys:roleList",
                        "sys:permissionList",
                        "sys:configList",
                        "sys:logList",
                        "sys:admin:list",
                        "sys:role:assignPerm",
                        "sys:config:delete",
                        "sys:log:delete"
                );

        assertThat(rolePermissionRepository.findByRoleId(1L))
                .extracting("permissionId")
                .containsAll(SEED_PERMISSION_IDS);

        assertThat(globalConfigRepository.findByConfigKey("global_download_path").orElseThrow().getConfigValue())
                .isEqualTo("/tmp/admin-base/download");
        assertThat(globalConfigRepository.findByConfigKey("global_upload_path").orElseThrow().getConfigValue())
                .isEqualTo("/tmp/admin-base/upload");
        assertThat(globalConfigRepository.findByConfigKey("sys_version").orElseThrow().getConfigValue())
                .isEqualTo("2.0.0");
    }

    /**
     * 测试通过种子数据中声明的权限（sys:adminList/sys:permissionList/sys:configList）能否驱动
     * MockMvc 成功访问角色列表、权限列表和全局配置列表端点，并验证响应 JSON 的关键字段符合预期。
     */
    @Test
    @WithMockUser(username = "admin", authorities = {
            "sys:adminList",
            "sys:permissionList",
            "sys:configList"
    })
    void seedV2AuthoritiesDriveListEndpoints() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"page":1,"size":10,"userName":"admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].userName").value("admin"))
                .andExpect(jsonPath("$.data.rows[0].roles[0].roleName").value("ROLE_ADMIN"));

        MvcResult permissionResult = mockMvc.perform(post("/permissions/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode permissionData = objectMapper.readTree(permissionResult.getResponse().getContentAsString())
                .path("data");
        assertThat(permissionData).isNotEmpty();
        assertThat(permissionData.findValuesAsText("perm")).contains("sys:adminList", "sys:permissionList");

        mockMvc.perform(post("/sys_global_config/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("key", "sys_version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].configKey").value("sys_version"))
                .andExpect(jsonPath("$.data.rows[0].configValue").value("2.0.0"));
    }

    /**
     * 向 tb_sys_admin 表插入或更新默认管理员账号（user_name=admin, admin_id=1, state=0）。
     * 该方法是安装种子数据的基础步骤。
     */
    private void seedAdmin() {
        jdbcTemplate.update("""
                INSERT INTO tb_sys_admin (admin_id, nickname, user_name, password, state, create_time, update_time)
                VALUES (1, ?, ?, ?, 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    nickname = VALUES(nickname),
                    user_name = VALUES(user_name),
                    password = VALUES(password),
                    state = VALUES(state),
                    update_time = VALUES(update_time)
                """, "管理员", "admin", "$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi");
    }

    /**
     * 向 tb_sys_role 表插入或更新 ROLE_ADMIN 角色（role_id=1），作为权限分配的载体。
     */
    private void seedRole() {
        jdbcTemplate.update("""
                INSERT INTO tb_sys_role (role_id, role_name, note, create_time, update_time)
                VALUES (1, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    role_name = VALUES(role_name),
                    note = VALUES(note),
                    update_time = VALUES(update_time)
                """, "ROLE_ADMIN", "管理员");
    }

    /**
     * 批量插入 26 条 v2 种子权限数据，覆盖菜单级（sys:adminList 等）和按钮级（sys:admin:add 等）
     * 两类权限，构成完整的权限树结构。
     */
    private void seedPermissions() {
        permission(1, 0, 1, "/system", "sys:manage", "system", "系统管理");
        permission(2, 1, 2, "/system/admin", "sys:adminList", "admin", "管理员列表");
        permission(7, 2, 3, "", "sys:admin:list", "", "管理员查询");
        permission(8, 2, 3, "", "sys:admin:add", "", "管理员新增");
        permission(9, 2, 3, "", "sys:admin:edit", "", "管理员编辑");
        permission(10, 2, 3, "", "sys:admin:delete", "", "管理员删除");
        permission(11, 2, 3, "", "sys:admin:resetPwd", "", "重置密码");
        permission(3, 1, 2, "/system/role", "sys:roleList", "role", "角色列表");
        permission(12, 3, 3, "", "sys:role:list", "", "角色查询");
        permission(13, 3, 3, "", "sys:role:add", "", "角色新增");
        permission(14, 3, 3, "", "sys:role:edit", "", "角色编辑");
        permission(15, 3, 3, "", "sys:role:delete", "", "角色删除");
        permission(16, 3, 3, "", "sys:role:assignPerm", "", "分配权限");
        permission(4, 1, 2, "/system/permission", "sys:permissionList", "permission", "权限列表");
        permission(17, 4, 3, "", "sys:permission:list", "", "权限查询");
        permission(18, 4, 3, "", "sys:permission:add", "", "权限新增");
        permission(19, 4, 3, "", "sys:permission:edit", "", "权限编辑");
        permission(20, 4, 3, "", "sys:permission:delete", "", "权限删除");
        permission(5, 1, 2, "/system/config", "sys:configList", "config", "全局配置");
        permission(21, 5, 3, "", "sys:config:list", "", "配置查询");
        permission(22, 5, 3, "", "sys:config:add", "", "配置新增");
        permission(23, 5, 3, "", "sys:config:edit", "", "配置编辑");
        permission(24, 5, 3, "", "sys:config:delete", "", "配置删除");
        permission(6, 1, 2, "/system/log", "sys:logList", "log", "操作日志");
        permission(25, 6, 3, "", "sys:log:list", "", "日志查询");
        permission(26, 6, 3, "", "sys:log:delete", "", "日志删除");
    }

    /**
     * 单条权限插入/更新辅助方法，向 tb_sys_permissions 表写入一条权限记录
     * （包含 id、父级 id、层级、路径、权限标识、URL、标题等字段）。
     *
     * @param id        权限主键
     * @param parentId  父级权限 id
     * @param level     权限层级（1=模块，2=菜单，3=按钮）
     * @param path      前端路由路径
     * @param perm      权限标识字符串
     * @param url       后端匹配 url
     * @param title     权限显示名
     */
    private void permission(long id, long parentId, int level, String path, String perm, String url, String title) {
        jdbcTemplate.update("""
                INSERT INTO tb_sys_permissions (permission_id, parent_id, level, path, perm, require_auth, state, url, title, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, 1, 1, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    parent_id = VALUES(parent_id),
                    level = VALUES(level),
                    path = VALUES(path),
                    perm = VALUES(perm),
                    require_auth = VALUES(require_auth),
                    state = VALUES(state),
                    url = VALUES(url),
                    title = VALUES(title),
                    update_time = VALUES(update_time)
                """, id, parentId, level, path, perm, url, title);
    }

    /**
     * 向 tb_sys_admin_role 表插入管理员与角色的关联记录（admin_id=1, role_id=1），
     * 把 admin 用户绑定到 ROLE_ADMIN 角色。
     */
    private void seedAdminRole() {
        jdbcTemplate.update("""
                INSERT INTO tb_sys_admin_role (admin_id, role_id, create_time)
                VALUES (1, 1, NOW())
                ON DUPLICATE KEY UPDATE role_id = VALUES(role_id)
                """);
    }

    /**
     * 遍历 SEED_PERMISSION_IDS，为 ROLE_ADMIN（role_id=1）逐一插入
     * tb_sys_role_permission 关联记录，使该角色拥有全部 26 个种子权限。
     */
    private void seedRolePermissions() {
        for (Long permissionId : SEED_PERMISSION_IDS) {
            jdbcTemplate.update("""
                    INSERT INTO tb_sys_role_permission (role_id, permission_id, create_time)
                    VALUES (1, ?, NOW())
                    ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id)
                    """, permissionId);
        }
    }

    /**
     * 插入 v2 种子脚本中的三条全局配置：文件下载路径、文件上传路径、系统版本号，
     * 用于测试全局配置相关接口的查询。
     */
    private void seedGlobalConfig() {
        globalConfig("global_download_path", "/tmp/admin-base/download", "文件下载路径");
        globalConfig("global_upload_path", "/tmp/admin-base/upload", "文件上传路径");
        globalConfig("sys_version", "2.0.0", "系统版本号");
    }

    /**
     * 单条全局配置插入/更新辅助方法，向 tb_sys_global_config 表写入一条配置项。
     *
     * @param key   配置键
     * @param value 配置值
     * @param note  配置说明
     */
    private void globalConfig(String key, String value, String note) {
        jdbcTemplate.update("""
                INSERT INTO tb_sys_global_config (config_key, config_value, note, create_time, update_time)
                VALUES (?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    config_value = VALUES(config_value),
                    note = VALUES(note),
                    update_time = VALUES(update_time)
                """, key, value, note);
    }
}
