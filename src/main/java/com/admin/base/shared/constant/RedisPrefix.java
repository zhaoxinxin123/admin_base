package com.admin.base.shared.constant;

/**
 * @author zhaoxin
 * @desc 缓存前缀
 */
public interface RedisPrefix {
    /**
     * 管理员缓存前缀，这里没使用（如需单点登录\其他需求，可使用）
     */
    String ADMIN_ID = "admin_id:";
    /**
     * Token缓存前缀，这里没使用（如需单点登录\其他需求，可使用）
     */
    String ADMIN_TOKEN = "token:";
    /**
     * 验证码前缀
     */
    String CAPTCHA_CODE_KEY = "captcha:";
    /**
     * 全局配置前缀
     */
    String GLOBAL_SETTING = "setting:";
}
