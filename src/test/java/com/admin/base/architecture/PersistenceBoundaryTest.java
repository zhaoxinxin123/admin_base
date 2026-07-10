package com.admin.base.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceBoundaryTest {

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
