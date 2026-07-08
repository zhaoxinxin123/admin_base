package com.admin.base.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/3/31 20:37
 * @desc 共用日期字段
 */
@Data
public class CommonDate {
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
