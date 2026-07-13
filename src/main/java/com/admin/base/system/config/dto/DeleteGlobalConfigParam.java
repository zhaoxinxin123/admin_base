package com.admin.base.system.config.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 8:46 下午
 * @desc
 */
@Data
public class DeleteGlobalConfigParam {

    @NotNull(message = "配置ID不能为空")
    private List<Integer> configIds;
}
