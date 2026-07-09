package com.admin.base.service.system;

import com.admin.base.entity.system.Permissions;
import com.admin.base.entity.system.Role;
import com.admin.base.entity.system.RolePermission;

import java.util.List;

/**
 * <p>
 *  角色权限服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface IRolePermissionService {
    /**
     * 根据角色Id删除对应的权限
     * @param roleId  角色id
     */
    void deleteByRoleId(Integer roleId);

    /**
     * 更新角色相关权限
     * @param roleId  角色id
     * @param permissionIds  权限列表
     */
    void updateRolePermission(Integer roleId, List<Integer> permissionIds);

    /**
     * 插入角色相关权限
     * @param roleId  角色id
     * @param permissionIds  权限列表
     */
    void insertRolePermission(Integer roleId, List<Integer> permissionIds);

    /**
     * 根据管理员角色Id  获取权限id列表
     * @param roleId  角色id
     * @return  权限Id列表
     */
    List<Integer> selectPermissionIdByRoleId(Integer roleId);

    /**
     * 根据角色查询权限
     * @param roles  角色列表
     * @return  权限列表
     */
    List<Permissions> selectPermissionByRoles(List<Role> roles);

    /**
     * 根据权限Id 删除角色权限
     * @param permissionId 权限Id
     */
    void deleteByPermissionId(Integer permissionId);

}
