package com.admin.base.dto.response.system;

import lombok.Data;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/16 9:48 上午
 * @desc 验证码
 */
@Data
public class CaptchaResponse {
    private String uuid;

    private String img;
    //临时变量测试使用
    private String tmpVar;
}
