package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 8:52 下午
 * @desc
 */
@Data
public class UpdateGlobalConfigParam {
    @NotNull(message = "configId 不能为空")
    private Integer configId;
    @NotEmpty(message = "参数备注不能为空")
    private String note;
    @NotEmpty(message = "key 不能为空")
    private String key;
    @NotEmpty(message = "value 不能为空")
    private String value;
}
