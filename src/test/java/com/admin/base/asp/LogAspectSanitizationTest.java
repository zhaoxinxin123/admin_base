package com.admin.base.infrastructure.aop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogAspectSanitizationTest {

    @Test
    void masksSensitiveJsonFields() {
        LogAspect aspect = new LogAspect();

        String result = aspect.sanitizeLogPayload("{\"username\":\"admin\",\"password\":\"secret\",\"token\":\"abc\"}");

        assertThat(result).contains("\"username\":\"admin\"");
        assertThat(result).contains("\"password\":\"***\"");
        assertThat(result).contains("\"token\":\"***\"");
        assertThat(result).doesNotContain("secret");
        assertThat(result).doesNotContain("abc");
    }
}