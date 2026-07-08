package com.admin.base.annotation;

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
