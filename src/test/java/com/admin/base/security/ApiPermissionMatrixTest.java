package com.admin.base.security;

import com.admin.base.infrastructure.controller.CommonController;
import com.admin.base.system.admin.controller.AdminController;
import com.admin.base.system.admin.controller.AdminRoleController;
import com.admin.base.system.config.controller.GlobalConfigController;
import com.admin.base.system.log.controller.OperationLogController;
import com.admin.base.system.permission.controller.PermissionsController;
import com.admin.base.system.role.controller.RoleController;
import com.admin.base.system.role.controller.RolePermissionController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiPermissionMatrixTest {

    private static final List<Class<?>> PROTECTED_CONTROLLERS = List.of(
            CommonController.class,
            AdminController.class,
            AdminRoleController.class,
            GlobalConfigController.class,
            OperationLogController.class,
            PermissionsController.class,
            RoleController.class,
            RolePermissionController.class
    );

    private static final Map<String, String> EXPECTED_PERMISSIONS = Map.ofEntries(
            Map.entry("GET /common/download", "hasAuthority('sys:file:download') and (!#delete or hasAuthority('sys:file:delete'))"),
            Map.entry("POST /common/upload", "hasAuthority('sys:file:upload')"),
            Map.entry("GET /common/download/resource2", "hasAuthority('sys:file:download')"),
            Map.entry("POST /admin/add", "hasAuthority('sys:admin:add')"),
            Map.entry("POST /admin/delete", "hasAuthority('sys:admin:delete')"),
            Map.entry("POST /admin/getMenu", "isAuthenticated()"),
            Map.entry("POST /admin_role/list", "hasAuthority('sys:adminList')"),
            Map.entry("POST /admin_role/resetPassword", "hasAuthority('sys:admin:resetPwd')"),
            Map.entry("POST /admin_role/updateState", "hasAuthority('sys:admin:edit')"),
            Map.entry("POST /admin_role/updateAdminOfRole", "hasAuthority('sys:admin:edit')"),
            Map.entry("POST /sys_global_config/list", "hasAuthority('sys:configList')"),
            Map.entry("POST /sys_global_config/add", "hasAuthority('sys:config:add')"),
            Map.entry("POST /sys_global_config/deleteBatch", "hasAuthority('sys:config:delete')"),
            Map.entry("POST /sys_global_config/update", "hasAuthority('sys:config:edit')"),
            Map.entry("POST /sys_operation_log/list", "hasAuthority('sys:logList')"),
            Map.entry("POST /sys_operation_log/deleteBatch", "hasAuthority('sys:log:delete')"),
            Map.entry("POST /permissions/add", "hasAuthority('sys:permission:add')"),
            Map.entry("POST /permissions/update", "hasAuthority('sys:permission:edit')"),
            Map.entry("POST /permissions/delete", "hasAuthority('sys:permission:delete')"),
            Map.entry("POST /permissions/list", "hasAuthority('sys:permissionList')"),
            Map.entry("POST /role/all", "hasAuthority('sys:roleList')"),
            Map.entry("POST /role/add", "hasAuthority('sys:role:add')"),
            Map.entry("POST /role/delete", "hasAuthority('sys:role:delete')"),
            Map.entry("POST /role/update", "hasAuthority('sys:role:edit')"),
            Map.entry("POST /role_permission/manageList", "hasAuthority('sys:roleList')")
    );

    @Test
    void everyProtectedApiUsesTheExactDocumentedPermission() {
        Map<String, String> actualPermissions = new LinkedHashMap<>();

        for (Class<?> controller : PROTECTED_CONTROLLERS) {
            String prefix = controller.getAnnotation(RequestMapping.class).value()[0];
            for (Method method : controller.getDeclaredMethods()) {
                String route = routeOf(prefix, method);
                if (route == null) {
                    continue;
                }
                PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
                assertThat(preAuthorize)
                        .as("%s must declare @PreAuthorize", route)
                        .isNotNull();
                actualPermissions.put(route, preAuthorize.value());
            }
        }

        assertThat(actualPermissions).containsExactlyInAnyOrderEntriesOf(EXPECTED_PERMISSIONS);
    }

    private String routeOf(String prefix, Method method) {
        GetMapping get = method.getAnnotation(GetMapping.class);
        if (get != null) {
            return "GET " + prefix + get.value()[0];
        }
        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) {
            return "POST " + prefix + post.value()[0];
        }
        return null;
    }
}
