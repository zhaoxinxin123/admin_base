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

    @Test
    void jwtModeLoadsJwtConfig() {
        contextRunner.withPropertyValues("admin.auth.mode=jwt")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityConfig.class);
                    assertThat(context).hasSingleBean(JwtSecurityConfig.class);
                    assertThat(context).doesNotHaveBean(OAuth2ResourceServerSecurityConfig.class);
                });
    }

    @Test
    void oauth2ModeLoadsOauth2Config() {
        contextRunner.withPropertyValues("admin.auth.mode=oauth2")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityConfig.class);
                    assertThat(context).hasSingleBean(OAuth2ResourceServerSecurityConfig.class);
                    assertThat(context).doesNotHaveBean(JwtSecurityConfig.class);
                });
    }

    @TestConfiguration
    static class SecurityTestBeans {

        @Bean
        UserDetailsService userDetailsService() {
            return username -> User.withUsername(username)
                    .password("{noop}password")
                    .authorities("sys:adminList")
                    .build();
        }

        @Bean
        HandlerExceptionResolver handlerExceptionResolver() {
            return mock(HandlerExceptionResolver.class);
        }

        @Bean
        InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint() {
            return new InvalidAuthenticationEntryPoint();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        JwtTokenUtil jwtTokenUtil() {
            return mock(JwtTokenUtil.class);
        }

        @Bean
        ICacheService cacheService() {
            return mock(ICacheService.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
            return new JwtAuthenticationConverter();
        }
    }
}
