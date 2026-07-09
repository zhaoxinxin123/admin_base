package com.admin.base.database;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDraftTest {

    @Test
    void schemaDraftContainsOnlyRetainedTablesAndNoForeignKeys() throws IOException {
        String ddl = Files.readString(Path.of("docs/database/admin-base-schema-v2.sql")).toLowerCase();

        assertThat(ddl).contains("create table tb_sys_admin");
        assertThat(ddl).contains("create table tb_sys_role");
        assertThat(ddl).contains("create table tb_sys_permissions");
        assertThat(ddl).contains("create table tb_sys_admin_role");
        assertThat(ddl).contains("create table tb_sys_role_permission");
        assertThat(ddl).contains("create table tb_sys_global_config");
        assertThat(ddl).contains("create table tb_sys_operation_log");
        assertThat(ddl).doesNotContain("create table tb_keys");
        assertThat(ddl).doesNotContain("create table tb_records");
        assertThat(ddl).doesNotContain("foreign key");
    }
}