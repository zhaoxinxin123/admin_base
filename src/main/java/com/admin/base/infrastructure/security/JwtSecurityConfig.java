package com.admin.base.infrastructure.security;

import com.admin.base.infrastructure.web.MyTokenFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "jwt", matchIfMissing = true)
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http,
                                              InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint,
                                              AuthenticationProvider authenticationProvider,
                                              MyTokenFilter myTokenFilter) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(invalidAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/open/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/open/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/druid/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/druid/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/prometheus/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(myTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MyTokenFilter myTokenFilter() {
        return new MyTokenFilter();
    }
}
