package com.admin.base.shared.util;

import com.admin.base.infrastructure.security.UserDetailsImpl;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.shared.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/22 10:11 下午
 * @desc
 */
public class SecurityUtils {
    /**
     * 用户ID
     **/
    public static Long getUserId() {
        try {
            return getLoginUser().getAdminId();
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.CODE_NO_LOGIN, "获取用户ID异常");
        }
    }


    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.CODE_NO_LOGIN, "获取用户账户异常");
        }
    }

    /**
     * 获取用户
     **/
    public static UserDetailsImpl getLoginUser() {
        final Authentication authentication = getAuthentication();
        final Object principal = authentication.getDetails();
        try {
            return (UserDetailsImpl) getAuthentication().getDetails();
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.CODE_NO_LOGIN, "获取用户信息异常");
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


}
