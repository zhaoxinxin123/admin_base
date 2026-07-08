package com.admin.base.dto.request.keys;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/1 15:06
 * @desc 
 */
@Data
public class DeleteKeyParam {
    @NotNull(message = "关键词ID不能为空")
    private Integer keyId;
}
