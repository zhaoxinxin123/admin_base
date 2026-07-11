package com.admin.base.system.role.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 1:45 下午
 * @desc
 */
@Data
public class AddRoleParam {
    /**
     * 角色名
     */
    @NotEmpty(message = "角色名不能为空！")
    private String roleName;

    /**
     * 权限id列表
     */
    @NotNull(message = "权限不能为空！")
    private List<Integer> permissionIds;
    /**
     * 备注
     */
    private String note ;
}
