package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 5:05 下午
 * @desc
 */
@Data
public class UpdateAdminPasswordParam {
    /**
     * 管理员id
     */
    @NotNull(message = "管理员ID不能为空！")
    private Integer adminId;
    /**
     * 密码
     */
    @NotNull(message = "密码不能为空！")
    @Pattern(regexp = "^(?=.*?[a-zA-Z])(?=.*?[0-9@!#*_])[a-zA-Z0-9@!#*_]{6,16}$", message = "请输入6-16位大、小写字母、数字或特殊字符(@!#*_)，必须至少包含其中两种类型")
    private String password;
}
