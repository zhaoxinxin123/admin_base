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

    @Test
    void defaultsToJwtWhenModeIsAbsent() {
        AuthModeProperties properties = new AuthModeProperties(null);

        assertThat(properties.mode()).isEqualTo(AuthModeProperties.AuthMode.JWT);
    }

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