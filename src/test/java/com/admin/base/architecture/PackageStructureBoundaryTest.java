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

        // 业务模块下必须按 controller/service/repository/entity 划分子包
        for (String module : List.of("admin", "role", "permission", "config", "log")) {
            assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("system/" + module + "/controller"))).isTrue();
            assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("system/" + module + "/service"))).isTrue();
            assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("system/" + module + "/repository"))).isTrue();
            assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("system/" + module + "/entity"))).isTrue();
        }

        // 旧的水平分层目录（按"技术"分）应当不存在
        assertThat(Files.exists(MAIN_PACKAGE.resolve("controller/system"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("service/system"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("repository/system"))).isFalse();
        assertThat(Files.exists(MAIN_PACKAGE.resolve("entity/system"))).isFalse();
    }

    @Test
    void systemBusinessCodeDoesNotUseLegacyLayerDirectories() {
        // 旧的 web/application/persistence/domain 命名必须彻底消失
        for (String legacy : List.of("web", "application", "persistence", "domain")) {
            assertThat(Files.exists(MAIN_PACKAGE.resolve("auth/" + legacy)))
                    .as("auth/" + legacy).isFalse();
            assertThat(Files.exists(MAIN_PACKAGE.resolve("system/admin/" + legacy)))
                    .as("system/admin/" + legacy).isFalse();
            assertThat(Files.exists(MAIN_PACKAGE.resolve("system/role/" + legacy)))
                    .as("system/role/" + legacy).isFalse();
            assertThat(Files.exists(MAIN_PACKAGE.resolve("shared/" + legacy)))
                    .as("shared/" + legacy).isFalse();
            assertThat(Files.exists(MAIN_PACKAGE.resolve("user/" + legacy)))
                    .as("user/" + legacy).isFalse();
        }

        // infrastructure/web 已被拆分为 controller/ 与 filter/
        assertThat(Files.exists(MAIN_PACKAGE.resolve("infrastructure/web"))).isFalse();
        assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("infrastructure/controller"))).isTrue();
        assertThat(Files.isDirectory(MAIN_PACKAGE.resolve("infrastructure/filter"))).isTrue();
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
