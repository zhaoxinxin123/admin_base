package com.admin.base.system.role.application;

import com.admin.base.shared.api.PageResult;
import com.admin.base.system.role.domain.Role;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface IRoleService {
    /**
     * 添加角色
     * @param roleName  角色名称
     * @param permissionIds  权限列表
     * @param note 备注
     */
    void addRole(String note,String roleName,List<Integer > permissionIds);

    /**
     * 删除角色
     * @param roleId  角色Id
     */
    void deleteRole(Integer roleId);

    /**
     * 修改角色
     * @param roleId  角色Id
     * @param roleName  角色名称
     * @param permissionIds   权限id列表
     */
    void updateRole(Integer roleId,String roleName,String note,List<Integer> permissionIds);

    /**
     * 检查角色名是否存在
     * @param roleName  角色名
     * @return  true  存在   false  不存在
     */
    boolean checkExitsByRoleName(String roleName);

    /**
     * 获取角色列表
     * @return  角色列表
     */
    List<Role> getRoleList();

    /**
     * 或与角色分页数据
     * @param page  当前页码
     * @param size  每页大小
     * @return   分页结果
     */
    PageResult<Role> getRolePage(Integer page,Integer size);

}
