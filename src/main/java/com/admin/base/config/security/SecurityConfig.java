package com.admin.base.config.security;

import com.admin.base.filter.MyTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/5 12:16 下午
 * @desc
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(AuthModeProperties.class)
public class SecurityConfig {

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Autowired
    private InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // OAuth2/OIDC 模式为后续实现计划预留，Phase 1 仅 JWT 模式为可执行安全链
        http
                // 禁用basic明文验证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 前后端分离架构不需要csrf保护
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用默认登录页
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用默认登出页
                .logout(AbstractHttpConfigurer::disable)
                // 设置异常的EntryPoint，如果不设置，默认使用Http403ForbiddenEntryPoint
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(invalidAuthenticationEntryPoint))
                // 前后端分离是无状态的，不需要session了，直接禁用。
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // 允许所有OPTIONS请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 允许直接访问授权登录接口
                        .requestMatchers(HttpMethod.POST, "/open/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/open/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/druid/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/druid/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/prometheus/**").permitAll()
                        // 允许 SpringMVC 的默认错误地址匿名访问
                        .requestMatchers("/error").permitAll()
                        // 其他所有接口必须有Authority信息，Authority在登录成功后的UserDetailsImpl对象中默认设置“ROLE_USER”
                        //.requestMatchers("/**").hasAnyAuthority("ROLE_USER")
                        // 允许任意请求被已登录用户访问，不检查Authority
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                // 加我们自定义的过滤器，替代UsernamePasswordAuthenticationFilter
                .addFilterBefore(myTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 调用 loadUserByUsername 获得 UserDetail 信息，
     * 在 AbstractUserDetailsAuthenticationProvider 里执行用户状态检查。
     * 直接复用已 @Autowired 注入的 UserDetailsServiceImpl 实例，
     * 避免与 @Service("userDetailsService") 注册的 bean 同名冲突。
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 密码 加密
     *
     * @return passwordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义token校验器
     *
     * @return filter
     */
    @Bean
    public MyTokenFilter myTokenFilter() {
        return new MyTokenFilter();
    }

}
