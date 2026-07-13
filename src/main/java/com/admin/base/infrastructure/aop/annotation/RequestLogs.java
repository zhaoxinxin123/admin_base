package com.admin.base.infrastructure.aop.annotation;

import java.lang.annotation.*;

/**
 * @author zhaoxin
 * @desc
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLogs {
    String value() default "";
}
