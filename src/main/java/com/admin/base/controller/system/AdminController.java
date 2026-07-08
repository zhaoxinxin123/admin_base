package com.admin.base.controller.system;


import com.admin.base.dto.request.system.AddAdminParam;
import com.admin.base.annotation.Log;
import com.admin.base.constant.log.BusinessType;
import com.admin.base.controller.common.BaseController;
import com.admin.base.common.JsonResponse;
import com.admin.base.dto.request.system.AdminIdParam;
import com.admin.base.dto.response.system.PermissionResponse;
import com.admin.base.entity.system.Permissions;
import com.admin.base.entity.system.Role;
import com.admin.base.service.system.IAdminRoleService;
import com.admin.base.service.system.IAdminService;
import com.admin.base.service.system.IRolePermissionService;
import com.admin.base.utils.ListEntityConvert;
import com.admin.base.utils.MenuUtils;
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
    @PreAuthorize("hasAuthority('sys:adminList:add')")
    @Log(title = "sys", businessType = BusinessType.INSERT)
    public JsonResponse addAdmin(@Validated AddAdminParam addAdminParam) {
        iAdminService.addAdmin(addAdminParam.getAccount(), addAdminParam.getPassword(), addAdminParam.getNickName(), addAdminParam.getRoleIds());
        return JsonResponse.success();
    }


    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('sys:adminList:delete')")
    @Log(title = "sys", businessType = BusinessType.INSERT)
    public JsonResponse deleteAdmin(@Validated AdminIdParam adminIdParam) {
        iAdminService.deleteAdmin(adminIdParam.getAdminId());
        return JsonResponse.success();
    }


    @PostMapping("/getMenu")
    public JsonResponse getMenu() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String userName = getUserName();
        List<Permissions> all;
        //根据用户名查询查询角色
        List<Role> roles = iAdminRoleService.selectRoleByName(userName);
        all = iRolePermissionService.selectPermissionByRoles(roles);

        final List<PermissionResponse> permissionResponses = ListEntityConvert.listCopyToAnotherList(PermissionResponse.class, all);
        assert permissionResponses != null;
        final List<PermissionResponse> rootMenus = permissionResponses.stream()
                .filter(item -> item.getLevel().equals(0))
                .collect(Collectors.toList());
        rootMenus.forEach(rootMenu ->
                rootMenu.setChild(MenuUtils.getChild(permissionResponses, rootMenu.getPermissionId(), rootMenu)));
        return JsonResponse.success(rootMenus);
    }


}

