package com.admin.base.database;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Schema 草案验证：确认 v2 仅包含 7 张系统核心表，无外键，无已删除的 tb_keys/tb_records。
 */
class SchemaDraftTest {

    @Test
    void schemaDraftContainsOnlyRetainedTablesAndNoForeignKeys() throws IOException {
        String ddl = Files.readString(Path.of("docs/database/admin-base-schema-v2.sql")).toLowerCase();

        // 确认 7 张系统核心表存在
        assertThat(ddl).contains("create table tb_sys_admin");
        assertThat(ddl).contains("create table tb_sys_role");
        assertThat(ddl).contains("create table tb_sys_permissions");
        assertThat(ddl).contains("create table tb_sys_admin_role");
        assertThat(ddl).contains("create table tb_sys_role_permission");
        assertThat(ddl).contains("create table tb_sys_global_config");
        assertThat(ddl).contains("create table tb_sys_operation_log");

        // 确认已删除的 tb_keys/tb_records 不在新 schema 中
        assertThat(ddl).doesNotContain("create table tb_keys");
        assertThat(ddl).doesNotContain("create table tb_records");

        // 确认不使用外键
        assertThat(ddl).doesNotContain("foreign key");
    }
}