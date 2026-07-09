package com.admin.base.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证模式配置属性，绑定 admin.auth 前缀。
 * Phase 1 仅 JWT 模式可执行，OAUTH2 模式为预留结构。
 */
@ConfigurationProperties(prefix = "admin.auth")
public record AuthModeProperties(AuthMode mode) {

    public AuthModeProperties {
        if (mode == null) {
            mode = AuthMode.JWT;
        }
    }

    public enum AuthMode {
        JWT,
        OAUTH2
    }
}