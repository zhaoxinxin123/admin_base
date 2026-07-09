package com.admin.base.config.security;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT token claims 的 Jackson 可序列化 DTO。
 * 仅携带 UserDetails 中必要的身份字段，避免直接对 UserDetailsImpl
 * 做 Jackson 反序列化（其构造器依赖 Admin/Role 等复杂类型）。
 */
public record TokenUser(
        String username,
        String password,
        String nickName,
        Integer adminId,
        List<String> perms
) {
    /**
     * 从 token payload 重建 UserDetailsImpl。
     * roles 不在 token 中持久化——每次请求由 UserDetailsService 从数据库重新加载。
     * roles 兜底为空列表，防止 getAuthorities() 遍历 null 导致 NPE。
     */
    public UserDetailsImpl toUserDetails() {
        UserDetailsImpl impl = new UserDetailsImpl();
        impl.setUsername(username);
        impl.setPassword(password);
        impl.setNickName(nickName);
        impl.setAdminId(adminId);
        impl.setPerms(perms);
        impl.setRoles(new ArrayList<>());
        return impl;
    }

    /**
     * 从完整 UserDetailsImpl 提取轻量 token payload。
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