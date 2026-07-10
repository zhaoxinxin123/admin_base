package com.admin.base.component;

import com.admin.base.dto.response.system.LoginResponse;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 11:20 上午
 * @desc
 */
public class ResponseInit {

    public static LoginResponse initLoginResponse(Long adminId,String userName,  String nickname, String token) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccount(userName);
        loginResponse.setAdminId(adminId);
        loginResponse.setNickName(nickname);
        loginResponse.setToken(token);
        return loginResponse;
    }
}
