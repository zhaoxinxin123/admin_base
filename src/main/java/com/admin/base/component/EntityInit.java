package com.admin.base.component;

import com.admin.base.constant.AdminStatus;
import com.admin.base.constant.YesOrNo;
import com.admin.base.dto.request.system.AddGlobalConfigParam;
import com.admin.base.dto.request.system.AddPermissionParam;
import com.admin.base.entity.keys.Keys;
import com.admin.base.entity.keys.Records;
import com.admin.base.entity.system.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/5 12:13 下午
 * @desc
 */
public class EntityInit {
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
        adminRole.setAdminId(adminId);
        adminRole.setRoleId(roleId);
        adminRole.setCreateTime(LocalDateTime.now());
        return adminRole;

    }

    public static RolePermission initRolePermission(Integer roleId, Integer addId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(addId);
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
        permissions.setParentId(addPermissionParam.getParentId());
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


    public static Keys initKeys(Integer userId,String keyWord, String note) throws NoSuchFieldException, IllegalAccessException {
        Keys keys = new Keys();
        keys.setKeyWord(keyWord);
        keys.setUserId(userId);
        keys.setNote(note);
        final LocalDateTime now = LocalDateTime.now();
        setDateFields(keys,now,now);
        return keys;
    }

    public static Records initRecords(String fileName, Integer userId, List<String> keyWords,String resultPath,boolean resultFlag) throws NoSuchFieldException, IllegalAccessException {
        Records records= new Records();
        records.setFileName(fileName);
        records.setUserId(userId);
        records.setKeyWordList(keyWords.toString());
        records.setResultPath(resultPath);
        records.setResultState(resultFlag?1:0);
        setDateFields(records,LocalDateTime.now(),LocalDateTime.now());
        return records;

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
}
