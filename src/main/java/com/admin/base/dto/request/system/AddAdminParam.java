package com.admin.base.dto.request.system;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 11:29 上午
 * @desc 新增管理员参数
 */
@Data
public class AddAdminParam {
    /**
     * 登录账号
     */
    @NotEmpty(message = "账号不能为空")
    private String account;
    /**
     * 登录密码
     */
    @NotEmpty(message = "密码不能为空")
    @Pattern(regexp = "(?!^([0-9]+|[a-zA-Z]+|[!#*_]+)$)^[a-zA-Z0-9@!#*_]{6,16}$",message = "请输入6-16位大、小写字母、数字或特殊字符(@!#*_)，必须至少包含其中两种类型")
    private String password;
    /**
     * 昵称
     */
    @NotEmpty(message = "用户昵称不能为空")
    private String nickName;
    /**
     * 角色id
     */
    @NotNull(message = "角色不能为空")
    private List<Integer> roleIds;

}
