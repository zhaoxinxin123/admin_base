package com.admin.base.shared.constant;

/**
 * @author zhaoxin
 * @desc 缓存时间
 */
public interface CacheTimeType {

    Integer ONE_MINUTE=60;
    /**
     * 1 小时
     */
    Integer ONE_HOUR = 60 * 60;
    /**
     * 2 小时
     */
    Integer TWO_HOUR = 2 * 60 * 60;
    /**
     * 3 小时
     */
    Integer THREE_HOUR = 3 * 60 * 60;
    /**
     * 4 小时
     */
    Integer FOUR_HOUR = 4 * 60 * 60;

    /**
     * 一天
     */
    Integer ONE_DAY = 24 * 60 * 60;

    /**
     * 两天
     */
    Integer TWO_DAY = 48 * 60 * 60;
}
