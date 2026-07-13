package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageStructureBoundaryTest {

    private static final Path MAIN_PACKAGE = Path.of("src/main/java/com/admin/base");

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
