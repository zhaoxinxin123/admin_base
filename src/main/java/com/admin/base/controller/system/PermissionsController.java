package com.admin.base.controller.system;


import com.admin.base.constant.log.BusinessType;
import com.admin.base.dto.request.system.DeletePermissionParam;
import com.admin.base.dto.request.system.UpdatePermissionParam;
import com.admin.base.annotation.Log;
import com.admin.base.controller.common.BaseController;
import com.admin.base.common.JsonResponse;
import com.admin.base.dto.request.system.AddPermissionParam;
import com.admin.base.dto.response.system.PermissionResponse;
import com.admin.base.entity.system.Permissions;
import com.admin.base.service.system.IPermissionsService;
import com.admin.base.utils.ListEntityConvert;
import com.admin.base.utils.MenuUtils;
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
