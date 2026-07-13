package com.admin.base.shared.factory;

import com.admin.base.shared.constant.AdminStatus;
import com.admin.base.shared.constant.YesOrNo;
import com.admin.base.system.admin.entity.Admin;
import com.admin.base.system.admin.entity.AdminRole;
import com.admin.base.system.config.dto.AddGlobalConfigParam;
import com.admin.base.system.config.entity.GlobalConfig;
import com.admin.base.system.permission.dto.AddPermissionParam;
import com.admin.base.system.permission.entity.Permissions;
import com.admin.base.system.role.entity.Role;
import com.admin.base.system.role.entity.RolePermission;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/5 12:13 下午
 * @desc
 */
public class EntityFactory {
    public static Admin initAdmin(String username, String password, String passwordShow, String nickName) {
        Admin admin = new Admin();
//        admin.setPasswordShow(passwordShow);
        //设置加密后的密码
        admin.setPassword(password);
        admin.setNickname(nickName);
        admin.setUserName(username);
        admin.setCreateTime(LocalDateTime.now());
        admin.setState(AdminStatus.COMMON);
        admin.setUpdateTime(LocalDateTime.now());
        return admin;
    }

    public static Role initRole(String roleName, String note) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setNote(note);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        return role;
    }

    public static AdminRole initAdminRole(Integer adminId, Integer roleId) {
        AdminRole adminRole = new AdminRole();
        adminRole.setAdminId(toLongId(adminId));
        adminRole.setRoleId(toLongId(roleId));
        adminRole.setCreateTime(LocalDateTime.now());
        return adminRole;

    }

    public static AdminRole initAdminRole(Long adminId, Integer roleId) {
        AdminRole adminRole = new AdminRole();
        adminRole.setAdminId(adminId);
        adminRole.setRoleId(toLongId(roleId));
        adminRole.setCreateTime(LocalDateTime.now());
        return adminRole;
    }

    public static RolePermission initRolePermission(Integer roleId, Integer addId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(toLongId(roleId));
        rolePermission.setPermissionId(toLongId(addId));
        rolePermission.setCreateTime(LocalDateTime.now());
        return rolePermission;
    }

    public static Permissions initPermission(AddPermissionParam addPermissionParam) {
        Permissions permissions = new Permissions();
        permissions.setCreateTime(LocalDateTime.now());
        permissions.setPerm(addPermissionParam.getPerm());
        permissions.setPath(addPermissionParam.getPath());
        permissions.setTitle(addPermissionParam.getTitle());
        permissions.setUrl(addPermissionParam.getIcon());
        permissions.setParentId(toLongId(addPermissionParam.getParentId()));
        permissions.setState(YesOrNo.YES);
        permissions.setRequireAuth(YesOrNo.YES);
        permissions.setUpdateTime(LocalDateTime.now());
        return permissions;

    }

    public static GlobalConfig initSysGlobalConfig(AddGlobalConfigParam addGlobalConfigParam) {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setConfigKey(addGlobalConfigParam.getKey());
        globalConfig.setConfigValue(addGlobalConfigParam.getValue());
        globalConfig.setNote(addGlobalConfigParam.getNote());
        globalConfig.setCreateTime(LocalDateTime.now());
        globalConfig.setUpdateTime(LocalDateTime.now());
        return globalConfig;
    }


    public static <T> void setDateFields(T entity, LocalDateTime createTime, LocalDateTime updateTime) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = entity.getClass();
        Field field1Obj = clazz.getDeclaredField("createTime");
        Field field2Obj = clazz.getDeclaredField("updateTime");
        field1Obj.setAccessible(true);
        field2Obj.setAccessible(true);
        field1Obj.set(entity, createTime);
        field2Obj.set(entity, updateTime);
    }

    private static Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }
}
