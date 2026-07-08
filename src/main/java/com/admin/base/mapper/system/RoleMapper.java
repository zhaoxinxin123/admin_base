package com.admin.base.mapper.system;

import com.admin.base.entity.system.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface RoleMapper extends BaseMapper<Role> {
    /**
     * 插入角色返回角色自增Id
     * @param role 角色
     * @return 自增id
     */
    Integer insertRole(Role role);
}
