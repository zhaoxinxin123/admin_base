package com.admin.base.annotation;

import com.admin.base.constant.log.BusinessType;

import java.lang.annotation.*;

/**
 * @author zhaoxin
 * @desc 自定义操作日志记录注解
 */
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块
     */
    String title()default "";

    /**
     * 功能
     *
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求日志
     */
    boolean isSaveRequestData() default true;
}
