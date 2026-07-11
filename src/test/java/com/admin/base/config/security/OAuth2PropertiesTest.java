package com.admin.base.config.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2PropertiesTest {

    @Test
    void defaultsClaimNames() {
        OAuth2Properties properties = new OAuth2Properties("", null, null, null);

        assertThat(properties.audience()).isEqualTo("admin-api");
        assertThat(properties.usernameClaim()).isEqualTo("preferred_username");
        assertThat(properties.authoritiesClaim()).isEqualTo("authorities");
    }
}
