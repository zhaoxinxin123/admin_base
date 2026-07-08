package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 1:48 下午
 * @desc
 */
@Data
public class UpdateRoleParam {
    /**
     * 角色id
     */
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;

    /**
     * 角色名
     */
    @NotEmpty(message = "角色名称不能为空！")
    private String roleName;

    /**
     * 备注
     */
    private String note;

    /**
     * 权限id列表
     */
    @NotNull(message = "权限不能为空！")
    private List<Integer> permissionIds;


}
