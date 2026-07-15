package com.admin.base.controller;

import com.admin.base.BaseApplication;
import com.admin.base.support.DevRemoteIntegrationTest;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseApplication.class)
@AutoConfigureMockMvc
@DevRemoteIntegrationTest
@TestPropertySource(properties = "admin.auth.mode=oauth2")
class OAuth2ResourceServerTest {

    private static final RSAKey SIGNING_KEY = createSigningKey();
    private static HttpServer oidcServer;
    private static String issuer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void oauth2Properties(DynamicPropertyRegistry registry) {
        startOidcServer();
        registry.add("admin.oauth2.issuer-uri", () -> issuer);
        registry.add("admin.oauth2.audience", () -> "admin-api");
        registry.add("admin.oauth2.username-claim", () -> "preferred_username");
        registry.add("admin.oauth2.authorities-claim", () -> "authorities");
    }

    @AfterAll
    static void stopOidcServer() {
        if (oidcServer != null) {
            oidcServer.stop(0);
        }
    }

    @Test
    void acceptsActuallySignedBearerTokenWithMappedAuthority() throws Exception {
        String token = signedToken("admin-api", List.of("sys:adminList"));

        mockMvc.perform(post("/admin_role/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void rejectsActuallySignedTokenWithWrongAudience() throws Exception {
        String token = signedToken("another-api", List.of("sys:adminList"));

        mockMvc.perform(post("/admin_role/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsBearerTokenWithoutRequiredAuthority() throws Exception {
        String token = signedToken("admin-api", List.of("sys:roleList"));

        mockMvc.perform(post("/admin_role/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("没有权限访问"));
    }

    @Test
    void recordsConfiguredOAuth2UsernameInOperationLog() throws Exception {
        String token = signedToken("admin-api", List.of("sys:config:add"));
        String configKey = "oauth.audit." + System.nanoTime();
        Long previousMaxId = jdbcTemplate.queryForObject(
                "select coalesce(max(operation_id), 0) from tb_sys_operation_log", Long.class);

        mockMvc.perform(post("/sys_global_config/add")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType("application/x-www-form-urlencoded")
                        .param("key", configKey)
                        .param("value", "enabled")
                        .param("note", "OAuth2 audit test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        Integer count = 0;
        while (System.nanoTime() < deadline) {
            count = jdbcTemplate.queryForObject("""
                    select count(*) from tb_sys_operation_log
                    where operation_id > ?
                      and operation_url = '/sys_global_config/add'
                      and operation_name = 'oauth-admin'
                    """, Integer.class, previousMaxId);
            if (count != null && count > 0) {
                break;
            }
            Thread.sleep(100);
        }
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void requiresBearerTokenForProtectedApi() throws Exception {
        mockMvc.perform(post("/admin_role/list")
                        .contentType("application/json")
                        .content("{\"page\":1,\"size\":10}"))
                .andExpect(status().isUnauthorized());
    }

    private static String signedToken(String audience, List<String> authorities) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("external-user-id")
                .audience(audience)
                .claim("preferred_username", "oauth-admin")
                .claim("authorities", authorities)
                .issueTime(Date.from(now.minusSeconds(5)))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(SIGNING_KEY.getKeyID())
                        .type(JOSEObjectType.JWT)
                        .build(),
                claims
        );
        jwt.sign(new RSASSASigner(SIGNING_KEY.toPrivateKey()));
        return jwt.serialize();
    }

    private static synchronized void startOidcServer() {
        if (oidcServer != null) {
            return;
        }
        try {
            oidcServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            issuer = "http://127.0.0.1:" + oidcServer.getAddress().getPort();
            oidcServer.createContext("/.well-known/openid-configuration", exchange -> writeJson(exchange, """
                    {"issuer":"%s","jwks_uri":"%s/jwks"}
                    """.formatted(issuer, issuer)));
            oidcServer.createContext("/jwks", exchange -> writeJson(
                    exchange, new JWKSet(SIGNING_KEY.toPublicJWK()).toString()));
            oidcServer.setExecutor(Executors.newCachedThreadPool());
            oidcServer.start();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start local OIDC test server", e);
        }
    }

    private static void writeJson(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static RSAKey createSigningKey() {
        try {
            return new RSAKeyGenerator(2048).keyID("admin-base-test-key").generate();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create OAuth2 test signing key", e);
        }
    }
}
