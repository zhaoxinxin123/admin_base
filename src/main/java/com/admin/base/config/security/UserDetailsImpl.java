package com.admin.base.config.security;

import com.admin.base.entity.system.Admin;
import com.admin.base.entity.system.Role;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 9:39 上午
 * @desc 实现UserDetails  接口
 */
@Data
public class UserDetailsImpl implements UserDetails {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 管理员ID
     */
    private Integer adminId;

    /**
     * 包含着用户对应的所有Role，在使用时调用者给对象注入roles
     */
    private List<Role> roles;
    /**
     * 包含着用户对应的所有权限，在使用时调用者给对象注入perms
     */
    private List<String> perms;


    public UserDetailsImpl(Admin admin, List<Role> roles, List<String> permissions) {
        this.password = admin.getPassword();
        this.username = admin.getUserName();
        this.nickName=admin.getNickname();
        this.adminId=admin.getAdminId();
        this.perms = permissions;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        for (String perm : perms) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 判断账号是否已经过期，默认没有过期
     *
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 判断账号是否被锁定，默认没有锁定
     *
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 判断信用凭证是否过期，默认没有过期
     *
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 判断账号是否可用，默认可用
     *
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
