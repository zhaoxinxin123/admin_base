package com.admin.base.mapper.system;

import com.admin.base.entity.system.Permissions;
import com.admin.base.entity.system.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    /**
     *  通过角色Id  获取权限id列表
     * @param roleId  角色Id
     * @return  权限id列表
     */
    List<Integer> selectPermissionIdByRoleId(Integer roleId);

    /**
     * 通过角色id列表获取权限列表
     * @param roleIds  角色id列表
     * @return 权限列表
     */
    List<Permissions> selectPermissionByRoles(List<Integer> roleIds);
}
