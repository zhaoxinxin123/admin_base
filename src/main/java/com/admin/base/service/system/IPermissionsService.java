package com.admin.base.service.system;

import com.admin.base.dto.request.system.UpdatePermissionParam;
import com.admin.base.dto.request.system.AddPermissionParam;
import com.admin.base.entity.system.Permissions;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface IPermissionsService {
    /**
     * 根据ids列表
     *
     * @param ids 权限Id列表
     * @return 权限列表
     */
    List<Permissions> getPermissionsByIds(List<Integer> ids);

    /**
     * 获取所有权限 (不包含被禁用的)
     *
     * @return 所有权限列表
     */
    List<Permissions> getAll();

    /**
     * 获取所有权限   （包含被禁用的）
     *
     * @return 所有权限列表
     */
    List<Permissions> getAllContainDisable();


    /**
     * 添加菜单
     *
     * @param addPermissionParam 添加菜单参数
     */
    void add(AddPermissionParam addPermissionParam);

    /**
     * 修改菜单
     * @param updatePermissionParam  更新权限参数
     */
    void updatePermission(UpdatePermissionParam updatePermissionParam);

    /**
     * 逻辑删除 菜单
     * @param permissionId
     */
    void deleteById(Integer permissionId);
}
