package com.admin.base.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaSeedConsistencyTest {

    /**
     * 测试 v2 schema 脚本：必须存在 tb_sys_admin 表和 uk_sys_admin_user_name 唯一索引，
     * 且不能包含 foreign key，也不能出现已废弃的 tb_keys / tb_records 表。
     */
    @Test
    void schemaUsesIndexesWithoutForeignKeys() throws Exception {
        String schema = Files.readString(Path.of("docs/database/admin-base-schema-v2.sql")).toLowerCase();

        assertThat(schema).contains("create table tb_sys_admin");
        assertThat(schema).contains("unique key uk_sys_admin_user_name");
        assertThat(schema).doesNotContain("foreign key");
        assertThat(schema).doesNotContain("tb_keys");
        assertThat(schema).doesNotContain("tb_records");
    }

    /**
     * 测试 v2 种子脚本只针对保留表（tb_sys_admin、tb_sys_role）插入数据，
     * 不能出现已废弃的 tb_keys / tb_records。
     */
    @Test
    void seedOnlyTargetsRetainedTables() throws Exception {
        String seed = Files.readString(Path.of("docs/database/admin-base-seed-v2.sql")).toLowerCase();

        assertThat(seed).contains("insert into tb_sys_admin");
        assertThat(seed).contains("insert into tb_sys_role");
        assertThat(seed).doesNotContain("tb_keys");
        assertThat(seed).doesNotContain("tb_records");
    }

    @Test
    void seedGrantsAdministratorAllFileAuthorities() throws Exception {
        String seed = Files.readString(Path.of("docs/database/admin-base-seed-v2.sql"));

        assertThat(seed)
                .contains("sys:file:upload")
                .contains("sys:file:download")
                .contains("sys:file:delete")
                .contains("(1, 28, NOW())")
                .contains("(1, 29, NOW())")
                .contains("(1, 30, NOW())");
    }

    /**
     * 测试 docs/database 目录只保留 v2 schema 和 v2 seed 两个脚本，
     * 并确保旧的 migration 脚本（admin-base-schema-v2-migration.sql）不存在。
     */
    @Test
    void databaseDirectoryContainsCurrentV2Scripts() {
        assertThat(Path.of("docs/database/admin-base-schema-v2.sql")).exists();
        assertThat(Path.of("docs/database/admin-base-seed-v2.sql")).exists();
        assertThat(Path.of("docs/database/admin-base-schema-v2-migration.sql")).doesNotExist();
    }
}
