package com.admin.base.dto.request.keys;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/3/31 20:20
 * @desc 添加关键词
 */
@Data
public class AddKeyParam {
    /**
     * 关键词
     */
    @NotEmpty(message = "关键词不能空")
    private String keyWord;
    /**
     * 备注
     */
    private String note;
}
