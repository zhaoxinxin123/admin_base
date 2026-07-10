package com.admin.base.utils;

import com.admin.base.config.security.TokenUser;
import com.admin.base.config.security.UserDetailsImpl;
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

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 7200L);
        ReflectionTestUtils.setField(jwtTokenUtil, "tokenHead", "Bearer ");
    }

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

    @Test
    void validateTokenReturnsTrueForValidToken() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setUsername("admin");
        userDetails.setPassword("pw");
        userDetails.setAdminId(1L);

        String token = jwtTokenUtil.generateToken(userDetails);
        assertThat(jwtTokenUtil.validateToken(token, userDetails)).isTrue();
    }

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
