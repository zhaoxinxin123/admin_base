package com.admin.base.mapper.system;

import com.admin.base.entity.system.AdminRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.admin.base.entity.system.Role;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface AdminRoleMapper extends BaseMapper<AdminRole> {
    /**
     * 根据管理员id查询角色
     * @param adminId   管理员id
     * @return 角色列表
     */
    List<Role> selectRoleByAdminId(Integer adminId);

}
