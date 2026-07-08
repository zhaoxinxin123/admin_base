package com.admin.base.mapper.system;

import com.admin.base.entity.system.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * tb_user Mapper 接口
 * </p>
 *
 * @author ZXX
 * @since 2021-08-19
 */
public interface AdminMapper extends BaseMapper<Admin> {
    /**
     * 插入获取自增id
     * @param admin  管理员id
     * @return  自增id
     */
    Integer insertAdmin(Admin admin);
}
