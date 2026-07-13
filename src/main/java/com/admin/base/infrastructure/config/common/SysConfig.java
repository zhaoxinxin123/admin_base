package com.admin.base.infrastructure.config.common;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/16 4:28 下午
 * @desc
 */
@Component
@ConfigurationProperties(prefix = "sysconfig")
public class SysConfig {
    /**
     * 验证码类型
     * -- GETTER --
     * //     * 资源映射路径前缀
     * //

     */
    @Getter
    private static String captchaType;
    /**
     * 上传路径
     */
    @Getter
    private static String uploadPath;
    /**
     * 通用下载路径
     */
    @Getter
    private static String downloadPath;
    /**
     * 本地资源下载路径
     */
    @Getter
    private static String localStore;

    public void setCaptchaType(String captchaType) {
        SysConfig.captchaType = captchaType;
    }

    public void setUploadPath(String uploadPath) {
        SysConfig.uploadPath = uploadPath;
    }

    public void setDownloadPath(String downloadPath) {
        SysConfig.downloadPath = downloadPath;
    }

    public void setLocalStore(String localStore) {
        SysConfig.localStore = localStore;
    }

}
