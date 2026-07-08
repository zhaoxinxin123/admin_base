package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 11:00 上午
 * @desc
 */
@Data
public class LoginParam {
    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空")
    private String username;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    private String password;
    /**
     * 验证码
     */
//    @NotEmpty(message = "验证码不能为空")
    private String code;

    /**
     * 验证码标识
     */
    @NotEmpty(message = "uuid 不能为空")
    private String uuid;
}
