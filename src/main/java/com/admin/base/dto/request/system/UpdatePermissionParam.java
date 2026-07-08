package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/15 9:31 上午
 * @desc
 */
@Data
public class UpdatePermissionParam {
    @NotNull(message = "permissionId 不能为空")
    private Integer permissionId;
    /**
     * 父级id
     */
    @NotNull(message = "parentId 不能为空")
    private Integer parentId;
    /**
     * 权限参数
     */
    @NotEmpty(message = "权限标识不能为空")
    private String perm;
    /**
     * 图标
     */
    private String icon;
    /**
     * 名称
     */
    private String name;
    /**
     * 菜单状态
     */
    @NotNull(message = "状态不能为空")
    private Integer state;

    /**
     * 标题
     */
    @NotEmpty(message = "标题不能为空")
    private String title;

    /**
     * 路径
     */
    @NotEmpty(message = "路径不能为空")
    private String path;
}
