package com.admin.base.shared.util;

import com.admin.base.infrastructure.security.TokenUser;
import com.admin.base.infrastructure.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for JwtTokenUtil with JJWT 0.12.6 API.
 * Uses ReflectionTestUtils to inject config values without Spring context.
 */
class JwtTokenUtilTest {

    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "test-jwt-secret-key-at-least-256-bits!!".getBytes());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private JwtTokenUtil jwtTokenUtil;

    /**
     * 在每个测试执行前新建 JwtTokenUtil 实例，并通过 ReflectionTestUtils 注入
     * Base64 编码的密钥、过期秒数（7200）和 Bearer 前缀，模拟 Spring 配置绑定。
     */
    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 7200L);
        ReflectionTestUtils.setField(jwtTokenUtil, "tokenHead", "Bearer ");
    }

    /**
     * 测试 JWT 往返一致性：通过 UserDetailsImpl 生成 token 后，
     * 应当能从 token 中读回用户名、密码、昵称、adminId、权限列表，
     * 并验证 getAuthorities() 不会因角色列表为空而抛 NPE。
     */
    @Test
    void generateTokenAndGetUserDetailRoundTrip() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setUsername("admin");
        userDetails.setPassword("encoded-password");
        userDetails.setNickName("管理员");
        userDetails.setAdminId(1L);
        userDetails.setPerms(List.of("sys:adminList", "sys:roleList"));

        String token = jwtTokenUtil.generateToken(userDetails);
        assertThat(token).isNotBlank();

        // Verify token carries username as subject
        String username = jwtTokenUtil.getUserNameFromToken(token);
        assertThat(username).isEqualTo("admin");

        // Verify round-trip: UserDetails → token → UserDetails
        UserDetails restored = jwtTokenUtil.getUserDetail(token);
        assertThat(restored).isNotNull();
        assertThat(restored.getUsername()).isEqualTo("admin");
        assertThat(restored.getPassword()).isEqualTo("encoded-password");

        UserDetailsImpl restoredImpl = (UserDetailsImpl) restored;
        assertThat(restoredImpl.getNickName()).isEqualTo("管理员");
        assertThat(restoredImpl.getAdminId()).isEqualTo(1L);
        assertThat(restoredImpl.getPerms()).containsExactly("sys:adminList", "sys:roleList");

        // Verify getAuthorities() does not throw NPE (roles defaulted to empty list)
        assertThat(restoredImpl.getAuthorities()).isNotNull();
        assertThat(restoredImpl.getAuthorities()).isNotEmpty();
    }

    /**
     * 测试使用合法 token 与对应的 UserDetails 调用 validateToken 时返回 true，
     * 验证签名、过期时间等校验通过。
     */
    @Test
    void validateTokenReturnsTrueForValidToken() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setUsername("admin");
        userDetails.setPassword("pw");
        userDetails.setAdminId(1L);

        String token = jwtTokenUtil.generateToken(userDetails);
        assertThat(jwtTokenUtil.validateToken(token, userDetails)).isTrue();
    }

    /**
     * 测试当传入的 UserDetails 用户名与 token 中的 subject 不一致时，
     * validateToken 返回 false，确保 token 不会被冒名顶替使用。
     */
    @Test
    void validateTokenReturnsFalseForWrongUsername() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setUsername("admin");
        userDetails.setPassword("pw");
        userDetails.setAdminId(1L);

        UserDetailsImpl other = new UserDetailsImpl();
        other.setUsername("other");
        other.setPassword("pw");
        other.setAdminId(2L);

        String token = jwtTokenUtil.generateToken(userDetails);
        assertThat(jwtTokenUtil.validateToken(token, other)).isFalse();
    }

    /**
     * 测试 TokenUser 经 Jackson 序列化再反序列化后字段保持一致，
     * 并验证 toUserDetails() 能将 TokenUser 正确转换为 UserDetailsImpl，
     * 保证 token payload 在不同阶段的契约稳定。
     */
    @Test
    void tokenUserRoundTripViaJackson() throws Exception {
        TokenUser original = new TokenUser("admin", "pw", "管理员", 1L,
                List.of("sys:adminList", "sys:roleList"));

        String json = objectMapper.writeValueAsString(original);
        TokenUser restored = objectMapper.readValue(json, TokenUser.class);

        assertThat(restored.username()).isEqualTo("admin");
        assertThat(restored.adminId()).isEqualTo(1L);
        assertThat(restored.perms()).containsExactly("sys:adminList", "sys:roleList");

        // Verify TokenUser → UserDetailsImpl conversion
        UserDetailsImpl impl = restored.toUserDetails();
        assertThat(impl.getUsername()).isEqualTo("admin");
        assertThat(impl.getAdminId()).isEqualTo(1L);
        assertThat(impl.getPerms()).containsExactly("sys:adminList", "sys:roleList");
    }
}
