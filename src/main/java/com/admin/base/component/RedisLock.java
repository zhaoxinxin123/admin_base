package com.admin.base.component;

import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/24 11:47 上午
 * @desc Redis 分布式锁
 */
@Data
@Component
public class RedisLock {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 加锁
     *
     * @param key
     * @param value 当前时间+超时时间
     * @return
     */
    public boolean lock(String key, String value, long time) {
        //相当于SETNX，setIfAbsent方法设置了为true,没有设置为false
        final Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
        if (success != null && success) {
            return true;
        }
        //假设currentValue=A
        String currentValue = String.valueOf(stringRedisTemplate.opsForValue().get(key));
        //如果锁过期  解决死锁
        if (!currentValue.isEmpty()
                && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            //获取上一个锁的时间，锁过期后，GETSET将原来的锁替换成新锁
            String oldValue = String.valueOf(stringRedisTemplate.opsForValue().getAndSet(key, value));
            return !oldValue.isEmpty() && oldValue.equals(currentValue);
        }
        //拿到锁的就有执行权力，拿不到的只有重新再来
        return false;
    }

    /**
     * 解锁
     *
     * @param key
     * @param value
     */
    public void unlock(String key, String value) {
        try {
            String currentValue = String.valueOf(stringRedisTemplate.opsForValue().get(key));
            if (!currentValue.isEmpty() && currentValue.equals(value)) {
                stringRedisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
