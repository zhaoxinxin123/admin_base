package com.admin.base.system.admin.application;

import com.admin.base.system.admin.domain.AdminRole;
import com.admin.base.system.role.domain.Role;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface IAdminRoleService {
    /**
     * 添加管理角色
     *
     * @param adminId 管理员Id
     * @param roleId  角色id
     */
    void addAdminRole(Long adminId, Integer roleId);

    /**
     * 查询管理员角色
     *
     * @param adminId 管理员id
     * @return 管理员角色列表
     */
    List<Role> selectByAdminId(Long adminId);

    /**
     * 根据登录名获取角色名
     *
     * @param userName 用户名
     * @return 角色列表
     */
    List<Role> selectRoleByName(String userName);

    /**
     * 修改管理员角色
     *
     * @param adminId 管理员Id
     * @param roleIds 角色ID
     */
    void updateAdminOfRole(Integer adminId, List<Integer> roleIds);
}
