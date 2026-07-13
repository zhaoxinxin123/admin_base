package com.admin.base.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2PropertiesTest {

    /**
     * 测试 OAuth2Properties 的默认值：当未显式配置时，audience 默认为 "admin-api"、
     * usernameClaim 默认为 "preferred_username"、authoritiesClaim 默认为 "authorities"，
     * 验证与 Spring Security 常见 OIDC 命名习惯一致。
     */
    @Test
    void defaultsClaimNames() {
        OAuth2Properties properties = new OAuth2Properties("", null, null, null);

        assertThat(properties.audience()).isEqualTo("admin-api");
        assertThat(properties.usernameClaim()).isEqualTo("preferred_username");
        assertThat(properties.authoritiesClaim()).isEqualTo("authorities");
    }
}
