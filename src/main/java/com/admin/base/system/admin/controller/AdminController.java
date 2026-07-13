package com.admin.base.system.admin.controller;


import com.admin.base.system.admin.dto.AddAdminParam;
import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.shared.constant.log.BusinessType;
import com.admin.base.infrastructure.controller.BaseController;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.system.admin.dto.AdminIdParam;
import com.admin.base.system.permission.dto.PermissionResponse;
import com.admin.base.system.permission.entity.Permissions;
import com.admin.base.system.role.entity.Role;
import com.admin.base.system.admin.service.IAdminRoleService;
import com.admin.base.system.admin.service.IAdminService;
import com.admin.base.system.role.service.IRolePermissionService;
import com.admin.base.shared.util.ListEntityConvert;
import com.admin.base.shared.util.MenuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@Slf4j
@RequestMapping("/admin")
@RestController
public class AdminController extends BaseController {
    @Resource
    private IAdminService iAdminService;
    @Resource
    private IAdminRoleService iAdminRoleService;
    @Resource
    private IRolePermissionService iRolePermissionService;


    @PostMapping("/add")
    @PreAuthorize("hasAuthority('sys:admin:add')")
    @Log(title = "sys", businessType = BusinessType.INSERT)
    public JsonResponse addAdmin(@Validated AddAdminParam addAdminParam) {
        iAdminService.addAdmin(addAdminParam.getAccount(), addAdminParam.getPassword(), addAdminParam.getNickName(), addAdminParam.getRoleIds());
        return JsonResponse.success();
    }


    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('sys:admin:delete')")
    @Log(title = "sys", businessType = BusinessType.INSERT)
    public JsonResponse deleteAdmin(@Validated AdminIdParam adminIdParam) {
        iAdminService.deleteAdmin(adminIdParam.getAdminId());
        return JsonResponse.success();
    }


    @PostMapping("/getMenu")
    @PreAuthorize("isAuthenticated()")
    public JsonResponse getMenu() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String userName = getUserName();
        List<Permissions> all;
        //根据用户名查询查询角色
        List<Role> roles = iAdminRoleService.selectRoleByName(userName);
        all = iRolePermissionService.selectPermissionByRoles(roles);

        final List<PermissionResponse> permissionResponses = ListEntityConvert.listCopyToAnotherList(PermissionResponse.class, all);
        assert permissionResponses != null;
        final List<PermissionResponse> rootMenus = permissionResponses.stream()
                .filter(item -> item.getParentId() != null && item.getParentId().equals(0L))
                .collect(Collectors.toList());
        rootMenus.forEach(rootMenu ->
                rootMenu.setChild(MenuUtils.getChild(permissionResponses, rootMenu.getPermissionId(), rootMenu)));
        return JsonResponse.success(rootMenus);
    }


}
