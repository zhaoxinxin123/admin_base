package com.admin.base.system.permission.web;


import com.admin.base.shared.constant.log.BusinessType;
import com.admin.base.system.permission.dto.DeletePermissionParam;
import com.admin.base.system.permission.dto.UpdatePermissionParam;
import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.infrastructure.web.BaseController;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.system.permission.dto.AddPermissionParam;
import com.admin.base.system.permission.dto.PermissionResponse;
import com.admin.base.system.permission.domain.Permissions;
import com.admin.base.system.permission.application.IPermissionsService;
import com.admin.base.shared.util.ListEntityConvert;
import com.admin.base.shared.util.MenuUtils;
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
 * 菜单管理
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@RestController
@RequestMapping("/permissions")
public class PermissionsController extends BaseController {

    @Resource
    private IPermissionsService iPermissionsService;


    @PostMapping("/add")
    @PreAuthorize("hasAuthority('sys:permission:add')")
    @Log(title = "sys", businessType = BusinessType.INSERT)
    public JsonResponse add(@Validated AddPermissionParam addPermissionParam) {
        iPermissionsService.add(addPermissionParam);
        return JsonResponse.success();
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sys:permission:update')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse update(@Validated UpdatePermissionParam updatePermissionParam) {
        iPermissionsService.updatePermission(updatePermissionParam);
        return JsonResponse.success();
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('sys:permission:delete')")
    @Log(title = "sys", businessType = BusinessType.DELETE)
    public JsonResponse delete(@Validated DeletePermissionParam deletePermissionParam) {
        iPermissionsService.deleteById(deletePermissionParam.getPermissionId());
        return JsonResponse.success();
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('sys:permissionList')")
    public JsonResponse list() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final List<Permissions> all = iPermissionsService.getAllContainDisable();
        //获取权限树
        final List<PermissionResponse> permissionResponses = ListEntityConvert.listCopyToAnotherList(PermissionResponse.class, all);
        //获取角色所有权限的ids
        assert permissionResponses != null;
        List<PermissionResponse> rootMenus = permissionResponses.stream()
                .filter(item -> item.getParentId() != null && item.getParentId().equals(0L))
                .collect(Collectors.toList());
        rootMenus.forEach(rootMenu ->
                rootMenu.setChild(MenuUtils.getChild(permissionResponses, rootMenu.getPermissionId(), rootMenu)));
        //检查该管理员下 是否选中 list
        return JsonResponse.success(rootMenus);
    }
}
