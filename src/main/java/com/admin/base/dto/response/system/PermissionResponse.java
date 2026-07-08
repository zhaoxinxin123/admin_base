package com.admin.base.dto.response.system;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/12 7:33 下午
 * @desc
 */
@Data
public class PermissionResponse {
    private Integer permissionId;
    /**
     * 级别
     */
    private Integer level;
    /**
     * 名称
     */
    private String name;
    /**
     * 路径
     */
    private String path;
    /**
     * 上级Id
     */
    private Integer parentId;

    /**
     * 权限名
     */
    private String perm;
    /**
     * 是否需要认证
     */
    private Integer requireAuth;
    /**
     * url 可以存放图标或者其他
     */
    private String url;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 状态
     */
    private Integer state;

    /**
     * 是否被选中
     */
    private Integer selected;
    /**
     * 导航栏标题
     */
    private String title;

    /**
     * 子列表
     */
    private List<PermissionResponse> child;
}
