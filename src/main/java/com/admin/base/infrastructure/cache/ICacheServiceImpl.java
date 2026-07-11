package com.admin.base.infrastructure.cache;

import com.admin.base.shared.constant.CacheTimeType;
import com.admin.base.shared.constant.RedisPrefix;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 11:11 上午
 * @desc
 */
@Service
public class ICacheServiceImpl implements ICacheService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void saveToken(String token, String userId) {
        deleteToken(userId);
        //添加token-userid映射
        stringRedisTemplate.opsForValue().set(RedisPrefix.ADMIN_ID + userId, token, CacheTimeType.TWO_HOUR, TimeUnit.SECONDS);
        //添加userId-token映射
        stringRedisTemplate.opsForValue().set(RedisPrefix.ADMIN_TOKEN + token, userId, CacheTimeType.TWO_HOUR, TimeUnit.SECONDS);
    }

    @Override
    public void deleteToken(String userId) {
        final String token = stringRedisTemplate.opsForValue().get(RedisPrefix.ADMIN_ID + userId);
        if (ObjectUtils.isEmpty(token)) {
            return;
        }
        stringRedisTemplate.delete(RedisPrefix.ADMIN_ID + userId);
        stringRedisTemplate.delete(RedisPrefix.ADMIN_TOKEN + token);
    }

    @Override
    public String getUserByToken(String token) {
        return stringRedisTemplate.opsForValue().get(RedisPrefix.ADMIN_TOKEN + token);

    }

    @Override
    public void saveKeyValueExpired(String key, String value, Integer time) {
        stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public void saveKeyValue(String key, String value) {
        deleteByKey(key);
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String getValueByKey(String verifyKey) {
        return stringRedisTemplate.opsForValue().get(verifyKey);
    }

    @Override
    public void deleteByKey(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public String getTokenById(String id) {
        return stringRedisTemplate.opsForValue().get(RedisPrefix.ADMIN_ID + id);
    }

}
