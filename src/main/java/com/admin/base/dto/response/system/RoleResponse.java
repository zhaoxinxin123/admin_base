package com.admin.base.dto.response.system;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 2:48 下午
 * @desc
 */
@Data
public class RoleResponse {
    /**
     * 角色Id
     */
    private Integer roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 备注
     */
    private String note;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 权限列表
     */
    private List<PermissionResponse> permissionResponses;
}
