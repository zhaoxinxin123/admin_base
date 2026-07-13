package com.admin.base.infrastructure.security;

import com.admin.base.infrastructure.cache.ICacheService;
import com.admin.base.shared.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthModeSecurityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    SecurityConfig.class,
                    JwtSecurityConfig.class,
                    OAuth2ResourceServerSecurityConfig.class,
                    SecurityTestBeans.class
            )
            .withPropertyValues(
                    "jwt.tokenHeader=Authorization",
                    "jwt.tokenHead=Bearer",
                    "jwt.secret=YnJvLXN0cmluZy1hZG1pbi1iYXNlLWp3dC1zZWNyZXQ=",
                    "jwt.expiration=7200"
            )
            .withAllowBeanDefinitionOverriding(true);

    /**
     * 测试当 admin.auth.mode=jwt 时，Spring 上下文只加载 SecurityConfig 与 JwtSecurityConfig，
     * 不会装配 OAuth2ResourceServerSecurityConfig，确保本地 JWT 模式互斥生效。
     */
    @Test
    void jwtModeLoadsJwtConfig() {
        contextRunner.withPropertyValues("admin.auth.mode=jwt")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityConfig.class);
                    assertThat(context).hasSingleBean(JwtSecurityConfig.class);
                    assertThat(context).doesNotHaveBean(OAuth2ResourceServerSecurityConfig.class);
                });
    }

    /**
     * 测试当 admin.auth.mode=oauth2 时，Spring 上下文装配 SecurityConfig 与
     * OAuth2ResourceServerSecurityConfig，而不加载 JwtSecurityConfig，验证两种安全配置
     * 通过 admin.auth.mode 互斥切换。
     */
    @Test
    void oauth2ModeLoadsOauth2Config() {
        contextRunner.withPropertyValues("admin.auth.mode=oauth2")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityConfig.class);
                    assertThat(context).hasSingleBean(OAuth2ResourceServerSecurityConfig.class);
                    assertThat(context).doesNotHaveBean(JwtSecurityConfig.class);
                });
    }

    /**
     * 测试专用的 Spring 配置类：通过 @Bean 提供 UserDetailsService、HandlerExceptionResolver、
     * JwtDecoder、JwtTokenUtil、ICacheService、ObjectMapper、JwtAuthenticationConverter 等桩对象，
     * 让 SecurityConfig/JwtSecurityConfig/OAuth2ResourceServerSecurityConfig 能在 ApplicationContextRunner
     * 中成功装配。
     */
    @TestConfiguration
    static class SecurityTestBeans {

        /**
         * 提供一个最小可用的 UserDetailsService，对任意用户名返回带 sys:adminList 权限的 User。
         */
        @Bean
        UserDetailsService userDetailsService() {
            return username -> User.withUsername(username)
                    .password("{noop}password")
                    .authorities("sys:adminList")
                    .build();
        }

        /**
         * 提供一个 Mock 的 HandlerExceptionResolver，满足 SecurityConfig 中 @Qualifier 依赖。
         */
        @Bean
        HandlerExceptionResolver handlerExceptionResolver() {
            return mock(HandlerExceptionResolver.class);
        }

        /**
         * 提供真实的 InvalidAuthenticationEntryPoint Bean，让 SecurityConfig 中的认证失败入口
         * 在测试上下文里也能被注入。
         */
        @Bean
        InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint() {
            return new InvalidAuthenticationEntryPoint();
        }

        /**
         * 提供一个 Mock 的 JwtDecoder，由测试自行桩出 decode 行为，模拟 OAuth2 资源服务器解码。
         */
        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }

        /**
         * 提供一个 Mock 的 JwtTokenUtil，避免 JwtSecurityConfig 在装配时真正调用 JJWT 接口。
         */
        @Bean
        JwtTokenUtil jwtTokenUtil() {
            return mock(JwtTokenUtil.class);
        }

        /**
         * 提供一个 Mock 的 ICacheService，屏蔽真实 Redis 依赖，使安全相关配置在无 Redis 时也能加载。
         */
        @Bean
        ICacheService cacheService() {
            return mock(ICacheService.class);
        }

        /**
         * 提供一个全新的 ObjectMapper Bean，覆盖 Spring Boot 自动装配，测试上下文里统一序列化行为。
         */
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        /**
         * 提供一个默认的 JwtAuthenticationConverter，供 OAuth2ResourceServerSecurityConfig 装配。
         */
        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
            return new JwtAuthenticationConverter();
        }
    }
}
