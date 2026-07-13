package com.admin.base.infrastructure.config;

import com.admin.base.infrastructure.security.AuthModeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuthModeProperties 配置绑定测试。
 * 验证默认值和显式配置的绑定行为。
 */
class AuthModePropertiesTest {

    /**
     * 测试当 admin.auth.mode 配置缺失时 AuthModeProperties 的 mode() 默认值是 JWT，
     * 验证系统在没有显式配置的情况下回退到本地 JWT 模式。
     */
    @Test
    void defaultsToJwtWhenModeIsAbsent() {
        AuthModeProperties properties = new AuthModeProperties(null);

        assertThat(properties.mode()).isEqualTo(AuthModeProperties.AuthMode.JWT);
    }

    /**
     * 测试通过 Spring Boot Binder 把 admin.auth.mode=oauth2 绑定到 AuthModeProperties，
     * 验证显式配置 OAUTH2 时属性值能正确反序列化。
     */
    @Test
    void bindsOauth2Mode() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("admin.auth.mode", "oauth2");

        AuthModeProperties properties = Binder.get(environment)
                .bind("admin.auth", Bindable.of(AuthModeProperties.class))
                .orElseThrow(() -> new IllegalStateException("admin.auth 配置绑定失败"));

        assertThat(properties.mode()).isEqualTo(AuthModeProperties.AuthMode.OAUTH2);
    }
}