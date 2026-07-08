package com.admin.base.dto.request.system;

import lombok.Data;

import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 11:34 上午
 * @desc 分配角色参数
 */
@Data
public class UpdateAdminOfRoleParam {
    /**
     * 管理员id
     */
    private Integer adminId;
    /**
     * 角色id
     */
    private List<Integer> roleIds;
}
