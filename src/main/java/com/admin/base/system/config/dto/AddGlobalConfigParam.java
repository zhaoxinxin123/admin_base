package com.admin.base.system.config.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 8:33 下午
 * @desc
 */
@Data
public class AddGlobalConfigParam {
    /**
     * 键值
     */
    @NotEmpty(message = "配置项不能为空")
    private String key;
    /**
     * 值
     */
    @NotEmpty(message = "配置值不能为空")
    private String value;
    /**
     * 备注
     */
    @NotEmpty(message = "备注不能为空")
    private String note;
}
