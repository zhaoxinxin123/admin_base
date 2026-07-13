package com.admin.base.system.log.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 2:48 下午
 * @desc 删除操作日志
 */
@Data
public class DeleteOperationLogParam {
    @NotNull(message = "logIds 不能为空")
    private List<Integer> logIds;

}
