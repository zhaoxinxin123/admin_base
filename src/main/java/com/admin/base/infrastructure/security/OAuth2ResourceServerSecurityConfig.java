package com.admin.base.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(prefix = "admin.auth", name = "mode", havingValue = "oauth2")
public class OAuth2ResourceServerSecurityConfig {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(OAuth2Properties properties) {
        if (properties.issuerUri() == null || properties.issuerUri().isBlank()) {
            throw new IllegalStateException("admin.oauth2.issuer-uri must be configured when admin.auth.mode=oauth2");
        }
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(properties.issuerUri());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(
                JwtValidators.createDefaultWithIssuer(properties.issuerUri()),
                new OAuth2AudienceValidator(properties)
        ));
        return decoder;
    }

    @Bean
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http,
                                                 InvalidAuthenticationEntryPoint invalidAuthenticationEntryPoint,
                                                 JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
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
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }
}
