package com.admin.base.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/24 10:28 上午
 * @desc
 */
@Aspect
@Component
@Slf4j
public class RequestLogsAspect {
    @Pointcut("@annotation(com.admin.base.infrastructure.aop.annotation.RequestLogs)")
    public void annotationPointcut() {

    }

    /**
     * @param joinPoint 反射参数
     */
    @Before("annotationPointcut()")
    public void beforePointcut(JoinPoint joinPoint) {
        //此处进入到方法前  可以实现一些业务逻辑.

    }

    /**
     * @param proceedingJoinPoint 反射参数
     * @return 执行方法
     * @throws Throwable 异常
     */
    @Around("annotationPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //获取方法签名
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        // 获取参数名称
        final String[] parameterNames = methodSignature.getParameterNames();
        //获取参数值
        final Object[] args = proceedingJoinPoint.getArgs();
        //打印参数
        log.info("参数名称：" + Arrays.toString(parameterNames));
        log.info("参数值：" + Arrays.toString(args));
        final long statTime = System.currentTimeMillis();
        final Object proceed = proceedingJoinPoint.proceed();
        log.info("方法执行时长：" + (System.currentTimeMillis() - statTime) * 1.0 / 1000);
        return proceed;
    }

    /**
     * 在切入点return内容之后切入内容（可以用来对处理返回值做一些加工处理）
     *
     * @param joinPoint 反射参数
     */
    @AfterReturning("annotationPointcut()")
    public void doAfterReturning(JoinPoint joinPoint) {
    }
}
