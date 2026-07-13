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
     * 测试 Controller 与 service 接口层不再依赖 MyBatis Plus：扫描 /controller/ 和
     * service/system 下的 IxxxService 接口源文件，断言不出现 com.baomidou.mybatisplus、
     * IPage<> 或 IService<> 引用，防止旧持久层类型泄漏到业务边界。
     */
    @Test
    void mybatisPlusDoesNotAppearInControllersOrServiceInterfaces() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/com/admin/base"))) {
            String combined = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/controller/")
                            || path.toString().matches(".*/system/[a-z_]+/service/I[^/]+\\.java"))
                    .map(this::read)
                    .reduce("", String::concat);

            assertThat(combined).doesNotContain("com.baomidou.mybatisplus");
            assertThat(combined).doesNotContain("IPage<");
            assertThat(combined).doesNotContain("IService<");
        }
    }

    /**
     * 测试主源码（src/main/java/com/admin/base）整体已不再依赖 MyBatis/MyBatis Plus：
     * 断言主目录所有 .java 文件中不出现 com.baomidou.mybatisplus 或 org.mybatis 引用，
     * 保证持久层统一由 Spring Data JPA 承担。
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

    /**
     * 读取一个 Java 源文件的全部内容为字符串，IO 异常包装为 IllegalStateException 抛出。
     */
    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
