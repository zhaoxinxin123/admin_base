package com.admin.base.infrastructure.cache;

import com.admin.base.system.admin.entity.Admin;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhaoxin
 * @desc
 */
public interface ICacheService {
    /**
     * 保存用户token
     *
     * @param token  token信息
     * @param userId 用户Id
     */
    void saveToken(String token, String userId);

    /**
     * 删除用token
     *
     * @param userId 用户Id
     */
    void deleteToken(String userId);

    /**
     * 根据用token 获取管理员信息
     *
     * @param token token信息
     * @return  管理员信息
     */
    String getUserByToken(String token) ;

    /**
     *  设置带过期时间的 key value
     * @param key   key
     * @param value value
     * @param time  过期时间  s
     */
    void saveKeyValueExpired(String key,String value,Integer time);

    /**
     *  设置 key value
     * @param key   key
     * @param value value
     */
    void saveKeyValue(String key,String value);

    /**
     * 根据key获取value
     * @param verifyKey  验证码
     * @return   值
     */
    String getValueByKey(String verifyKey);

    /**
     * 根据key删除缓存
     * @param key 键值
     */
    void deleteByKey(String key);

    String getTokenById(String username);
}
