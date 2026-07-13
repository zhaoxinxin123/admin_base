package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageStructureBoundaryTest {

    private static final Path MAIN_PACKAGE = Path.of("src/main/java/com/admin/base");

    /**
     * 测试业务代码已迁移到按特性（feature）划分的包结构：system/admin、system/role、
     * system/permission、system/config、system/log 这五个目录必须存在；
     * 同时确认旧的水平分层目录（controller/system、service/system 等）已彻底移除。
     */
    @Test
    void systemBusinessCodeUsesFeaturePackages() {
        List<Path> expectedFeaturePackages = List.of(
                MAIN_PACKAGE.resolve("system/admin"),
                MAIN_PACKAGE.resolve("system/role"),
                MAIN_PACKAGE.resolve("system/permission"),
                MAIN_PACKAGE.resolve("system/config"),
                MAIN_PACKAGE.resolve("system/log")
        );

        for (Path path : expectedFeaturePackages) {
            assertThat(Files.isDirectory(path)).as(path.toString()).isTrue();
        }

        assertThat(Files.exists(MAIN_PACKAGE.resolve("controller/system"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("service/system"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("repository/system"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("entity/system"))).isFalse();
    }

    /**
     * 测试新引入的 shared（公共契约/异常/util 等）和 infrastructure（AOP/缓存/安全等）两个包存在，
     * 同时确认旧的水平技术分层目录 common、component、asp、filter、manager 已被废弃。
     */
    @Test
    void sharedAndInfrastructurePackagesReplaceHorizontalTechnicalBuckets() {
        assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("shared"))).isTrue();
        assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("infrastructure"))).isTrue();

        assertThat(Files.exists(MAIN_PACKAGE.resolve("common"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("component"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("asp"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("filter"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("manager"))).isFalse();
    }
}
