package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 2:02 下午
 * @desc
 */
@Data
public class DeleteRoleParam {
    /**
     * 角色id
     */
    @NotNull(message = "roleId 不能为空")
    private Integer roleId;
}
