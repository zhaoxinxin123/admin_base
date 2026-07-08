package com.admin.base.dto.request.keys;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/3 12:00
 * @desc
 */
@Data
public class DeleteRecordParam {
    @NotNull(message = "Id 不能为空！")
    private Integer id;
}
