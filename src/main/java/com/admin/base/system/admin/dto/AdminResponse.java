package com.admin.base.system.admin.dto;

import com.admin.base.system.role.dto.RoleResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 4:04 下午
 * @desc
 */
@Data
public class AdminResponse {
    private Long adminId;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 登录名
     */
    private String userName;
    /**
     * 状态
     */
    private Integer state;

    /**
     * 密码
     */
    private String passwordShow;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    List<RoleResponse> roles;
    /**
     * 角色Id
     */
    private Long roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色备注
     */
    private String note;

}
