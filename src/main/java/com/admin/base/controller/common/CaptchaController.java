package com.admin.base.controller.common;


import cn.hutool.captcha.LineCaptcha;
import com.admin.base.common.JsonResponse;
import com.admin.base.constant.RedisPrefix;
import com.admin.base.dto.response.system.CaptchaResponse;
import com.admin.base.service.ICacheService;
import com.admin.base.utils.CaptchaUtils;
import com.admin.base.utils.sign.Base64;
import com.admin.base.utils.uuid.IdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.awt.image.BufferedImage;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/15 2:30 下午
 * @desc
 */
@RestController
@Slf4j
@RequestMapping(value = "/open")
public class CaptchaController {
    @Resource
    private ICacheService iCacheService;


    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public JsonResponse getCode() {

        //可加限制，一般手机验证🐴会加限制
        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = RedisPrefix.CAPTCHA_CODE_KEY + uuid;

        String  code = null;
        LineCaptcha lineCaptcha = CaptchaUtils.str();
        iCacheService.saveKeyValueExpired(verifyKey, lineCaptcha.getCode(), 20);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        lineCaptcha.write(os);
        code=lineCaptcha.getCode();
        CaptchaResponse captchaResponse = new CaptchaResponse();
        captchaResponse.setImg(Base64.encode(os.toByteArray()));
        captchaResponse.setUuid(uuid);
        captchaResponse.setTmpVar(code);
        log.info(uuid + "--------" + code);
        return JsonResponse.success(captchaResponse);
    }
}
