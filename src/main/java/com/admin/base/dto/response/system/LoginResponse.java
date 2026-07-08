package com.admin.base.dto.response.system;

import lombok.Data;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 10:58 上午
 * @desc
 */
@Data
public class LoginResponse {
    /**
     * 登录成功的token
     */
    private String token;

    /**
     * 账号
     */
    private String account;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 管理员id
     */
    private Integer adminId;
}
