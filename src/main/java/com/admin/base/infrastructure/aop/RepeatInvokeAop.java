package com.admin.base.infrastructure.aop;

import com.admin.base.infrastructure.aop.annotation.RepeatInvoke;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.infrastructure.cache.RedisLock;
import com.admin.base.shared.constant.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/24 11:12 上午
 * @desc 防止重复提交切面
 */
@Aspect
@Component
@Slf4j
public class RepeatInvokeAop {
    @Resource
    private RedisLock redisLock;

    @Pointcut("@annotation(com.admin.base.infrastructure.aop.annotation.RepeatInvoke)")
    public void annotationPointcut() {

    }

    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //获取签名
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        //获取方法
        final Method method = methodSignature.getMethod();
        //获取方法的注解
        final RepeatInvoke annotation = method.getAnnotation(RepeatInvoke.class);
        //获取锁过期的时间
        final int time = annotation.time();
//        //获取登录token   JWT获取token
        final String token = "RequestUtils.getToken(currentRequest)";
        //重复请求的key
        String key = annotation.key() + token;
        String value = String.valueOf(System.currentTimeMillis() + time * 1000L);
        Object proceed;
        try {
            final boolean isSuccess = redisLock.lock(key, value, time);
            if (isSuccess) {
                //获取锁成功

                proceed = proceedingJoinPoint.proceed();
                return proceed;
            } else {
                //获取锁失败
                return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "获取锁失败！，");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            redisLock.unlock(key, value);
        }
        return null;
    }

    private String getClientId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
