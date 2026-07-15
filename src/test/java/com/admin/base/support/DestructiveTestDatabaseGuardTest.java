package com.admin.base.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DestructiveTestDatabaseGuardTest {

    @Test
    void acceptsDedicatedIntegrationDatabase() {
        assertThatCode(() -> DestructiveTestDatabaseGuard.requireIsolatedDatabase(
                "jdbc:mysql://192.168.3.3:3306/admin_base_it?useSSL=false"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsSharedApplicationDatabase() {
        assertThatThrownBy(() -> DestructiveTestDatabaseGuard.requireIsolatedDatabase(
                "jdbc:mysql://192.168.3.3:3306/admin_base?useSSL=false"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("_it");
    }

    @Test
    void rejectsMalformedJdbcUrl() {
        assertThatThrownBy(() -> DestructiveTestDatabaseGuard.requireIsolatedDatabase("not-a-jdbc-url"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JDBC");
    }
}
