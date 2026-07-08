package com.admin.base.dto.request.system;

import com.admin.base.constant.AdminStatus;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 5:37 下午
 * @desc
 */
@Data
public class UpdateAdminStateParam {
    /**
     * 管理员id
     */
    @NotNull(message = "管理员ID不能为空")
    private Integer adminId;
    /**
     * 状态
     * {@link AdminStatus}
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0)
    @Max(value = 1)
    private Integer state;
}
