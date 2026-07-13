package com.admin.base.infrastructure.aop.annotation;

import java.lang.annotation.*;

/**
 * @author zhaoxin
 * @desc  防止重复提交
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatInvoke {
    /**
     *
     * @return  时间
     */
    int time() default 10;

    /**
     * 防止重复提交的key
     */
    String key();

}
