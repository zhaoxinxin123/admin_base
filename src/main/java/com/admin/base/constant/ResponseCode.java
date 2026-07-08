package com.admin.base.constant;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/19 11:31 上午
 * @desc
 */
public interface ResponseCode {
    /**
     * 成功
     */
    int CODE_OK = 200;
    /**
     * 服务异常
     */
    int CODE_SYS_ERROR=500;

    /**
     * token
     */
    int CODE_TOKEN_ERROR = 401;

    /**
     * 未登录
     */
    Integer CODE_NO_LOGIN = 1003;
    /**
     * 弹窗
     */
    Integer CODE_ALERT = 1001;
}
