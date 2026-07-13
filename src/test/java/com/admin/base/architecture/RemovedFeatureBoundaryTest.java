package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RemovedFeatureBoundaryTest {

    /**
     * 测试已删除的功能模块不会再次出现在主源码中：entity/keys、dto/request/keys、
     * dto/response/keys 这三个被移除的 keys 目录以及工具类 utils/Boyer 都不应再存在。
     */
    @Test
    void removedFeaturePackagesDoNotExist() {
        List<Path> removedPaths = List.of(
                Path.of("src/main/java/com/admin/base/entity/keys"),
                Path.of("src/main/java/com/admin/base/dto/request/keys"),
                Path.of("src/main/java/com/admin/base/dto/response/keys"),
                Path.of("src/main/java/com/admin/base/utils/Boyer")
        );

        for (Path path : removedPaths) {
            assertThat(Files.exists(path)).as(path.toString()).isFalse();
        }
    }
}