package com.admin.base.dto.request.common;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/1 10:04
 * @desc
 */
@Data
public class PageParam {
    /**
     * 页码
     */
    @NotNull(message = "page 不能为空")
    private Integer page;
    /**
     * 每页大小
     */
    @NotNull(message = "size 不能为空")
    private Integer size;


}
