package com.admin.base.system.permission.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/15 10:04 上午
 * @desc
 */
@Data
public class DeletePermissionParam {
    @NotNull(message = " permissionId 不能为空")
    private Integer permissionId;
}
