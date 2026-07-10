package com.admin.base.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaSeedConsistencyTest {

    @Test
    void schemaUsesIndexesWithoutForeignKeys() throws Exception {
        String schema = Files.readString(Path.of("docs/database/admin-base-schema-v2.sql")).toLowerCase();

        assertThat(schema).contains("create table tb_sys_admin");
        assertThat(schema).contains("unique key uk_sys_admin_user_name");
        assertThat(schema).doesNotContain("foreign key");
        assertThat(schema).doesNotContain("tb_keys");
        assertThat(schema).doesNotContain("tb_records");
    }

    @Test
    void seedOnlyTargetsRetainedTables() throws Exception {
        String seed = Files.readString(Path.of("docs/database/admin-base-seed-v2.sql")).toLowerCase();

        assertThat(seed).contains("insert into tb_sys_admin");
        assertThat(seed).contains("insert into tb_sys_role");
        assertThat(seed).doesNotContain("tb_keys");
        assertThat(seed).doesNotContain("tb_records");
    }

    @Test
    void migrationOnlyTargetsRetainedTablesWithoutForeignKeys() throws Exception {
        String migration = Files.readString(Path.of("docs/database/admin-base-schema-v2-migration.sql")).toLowerCase();

        assertThat(migration).contains("alter table tb_sys_admin");
        assertThat(migration).contains("alter table tb_sys_operation_log");
        assertThat(migration).doesNotContain("foreign key");
        assertThat(migration).doesNotContain("tb_keys");
        assertThat(migration).doesNotContain("tb_records");
    }
}
