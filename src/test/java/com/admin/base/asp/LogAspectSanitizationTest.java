package com.admin.base.infrastructure.aop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogAspectSanitizationTest {

    /**
     * 测试 LogAspect 的 sanitizeLogPayload 方法：JSON 字符串中的 password、token
     * 等敏感字段应被替换为 ***，而普通字段（如 username）保持原值不脱敏。
     */
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