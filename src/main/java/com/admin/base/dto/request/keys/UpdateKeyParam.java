package com.admin.base.dto.request.keys;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/1 09:55
 * @desc 更新关键词参数
 */
@Data
public class UpdateKeyParam {
    /**
     * 关键词ID
     */
    @NotNull(message = "id不能为空")
    private Integer id;
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
