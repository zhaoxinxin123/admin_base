package com.admin.base.system.permission.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 7:00 下午
 * @desc
 */
@Data
public class AddPermissionParam {
    /**
     * 父级id
     */
    @NotNull(message = "父级ID不能为空")
    private Integer parentId;
    /**
     * 权限参数
     */
    @NotEmpty(message = "权限不能为空")
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
     * 导航栏标题
     */
    @NotEmpty(message = "标题不能为空")
    private String title;
    /**
     * 路径
     */
    @NotEmpty(message = "路径不能为空")
    private String path;
}
