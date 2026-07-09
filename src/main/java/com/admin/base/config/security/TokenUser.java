package com.admin.base.config.security;

import java.util.List;

/**
 * Jackson-friendly DTO for JWT token claims serialization.
 * Carries the essential UserDetails fields needed for token
 * reconstruction, avoiding direct Jackson deserialization of
 * UserDetailsImpl which has a non-default constructor.
 */
public record TokenUser(
        String username,
        String password,
        String nickName,
        Integer adminId,
        List<String> perms
) {
    /**
     * Reconstruct a full UserDetailsImpl from this token payload.
     * Roles are not persisted in the token — they are reloaded from
     * the database on each request via UserDetailsService.
     */
    public UserDetailsImpl toUserDetails() {
        UserDetailsImpl impl = new UserDetailsImpl();
        impl.setUsername(username);
        impl.setPassword(password);
        impl.setNickName(nickName);
        impl.setAdminId(adminId);
        impl.setPerms(perms);
        return impl;
    }

    /**
     * Extract a compact token payload from a full UserDetailsImpl.
     */
    public static TokenUser from(UserDetailsImpl userDetails) {
        return new TokenUser(
                userDetails.getUsername(),
                userDetails.getPassword(),
                userDetails.getNickName(),
                userDetails.getAdminId(),
                userDetails.getPerms()
        );
    }
}