package com.admin.base.infrastructure.security;

import com.admin.base.shared.constant.AdminStatus;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.system.admin.domain.Admin;
import com.admin.base.system.permission.domain.Permissions;
import com.admin.base.system.role.domain.Role;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.admin.application.IAdminRoleService;
import com.admin.base.system.admin.application.IAdminService;
import com.admin.base.system.role.application.IRolePermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/13 9:35 上午
 * @desc 实现UserDetailsService  根据用户名加载信息
 */
@Slf4j
@Service(value = "userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private IAdminService iAdminService;
    @Resource
    private IAdminRoleService iAdminRoleService;
    @Resource
    private IRolePermissionService iRolePermissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("查找用户：" + username);
        //查询用户
        Admin admin = iAdminService.selectByUserName(username);
        if (admin.getState().equals(AdminStatus.DISABLE)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "账号已被封禁，请联系管理员！");
        }
        //查询该用户角色
        final List<Role> roles = iAdminRoleService.selectByAdminId(admin.getAdminId());

        //角色拥有的权限
        List<Permissions> permissions = iRolePermissionService.selectPermissionByRoles(roles);
        final List<String> permissionsName = permissions.stream().map(Permissions::getPerm).collect(Collectors.toList());
        return new UserDetailsImpl(admin, roles, permissionsName);
    }
}
