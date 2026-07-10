package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 2 持久层边界测试。
 *
 * <p>这些测试不验证具体业务结果，而是防止迁移后控制器、服务接口或主代码重新依赖
 * MyBatis Plus 类型，保证外部 API 边界不泄漏旧持久层实现。</p>
 */
class PersistenceBoundaryTest {

    /**
     * Controller 和 service 接口是业务边界，不应暴露 MyBatis Plus 的分页或 IService 类型。
     */
    @Test
    void mybatisPlusDoesNotAppearInControllersOrServiceInterfaces() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/com/admin/base"))) {
            String combined = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/controller/")
                            || path.toString().matches(".*service/system/I[^/]+\\.java"))
                    .map(this::read)
                    .reduce("", String::concat);

            assertThat(combined).doesNotContain("com.baomidou.mybatisplus");
            assertThat(combined).doesNotContain("IPage<");
            assertThat(combined).doesNotContain("IService<");
        }
    }

    /**
     * 主源码应完全移除 MyBatis/MyBatis Plus 依赖，持久层统一由 Spring Data JPA 承担。
     */
    @Test
    void mybatisPlusIsRemovedFromMainSources() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/com/admin/base"))) {
            String combined = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::read)
                    .reduce("", String::concat);

            assertThat(combined).doesNotContain("com.baomidou.mybatisplus");
            assertThat(combined).doesNotContain("org.mybatis");
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
