package com.admin.base.mapper.system;

import com.admin.base.entity.system.Permissions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface PermissionsMapper extends BaseMapper<Permissions> {
    /**
     * 根据角色Id  获取权限列表
     * @param roleId   角色Id
     * @return  权限列表
     */
    List<Permissions> getPermissionByRoleId(@Param("roleId") Integer roleId);
}
