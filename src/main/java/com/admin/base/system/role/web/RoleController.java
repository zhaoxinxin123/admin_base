package com.admin.base.system.role.web;

import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.shared.constant.log.BusinessType;
import com.admin.base.infrastructure.web.BaseController;
import com.admin.base.system.role.dto.AddRoleParam;
import com.admin.base.system.role.dto.DeleteRoleParam;
import com.admin.base.system.role.dto.UpdateRoleParam;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.role.application.IRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 权限
 *
 * @author ZXX
 * @since 2021-09-05
 */
@RestController
@RequestMapping("/role")
public class RoleController extends BaseController {
    @Resource
    private IRoleService iRoleService;

    /**
     * 获取角色列表，为管理员分配角色时使用
     *
     * @return 角色列表
     */
    @PostMapping("/all")
    @PreAuthorize("hasAuthority('sys:roleList')")
    public JsonResponse getAllRole() {
        return JsonResponse.success(iRoleService.getRoleList());
    }


    /**
     * 添加角色
     *
     * @param addRoleParam 添加角色参数
     * @return 响应
     */
    @PostMapping("/add")
    @Log(title = "sys", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('sys:role:add')")
    public JsonResponse addRole(@Validated AddRoleParam addRoleParam) {
        checkRoleName(addRoleParam.getRoleName());
        iRoleService.addRole(addRoleParam.getNote(), addRoleParam.getRoleName(), addRoleParam.getPermissionIds());
        return JsonResponse.success();
    }


    /**
     * 删除角色
     *
     * @param deleteRoleParam 删除角色参数
     * @return 响应
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @Log(title = "sys", businessType = BusinessType.DELETE)
    public JsonResponse deleteRole(@Validated DeleteRoleParam deleteRoleParam) {
        iRoleService.deleteRole(deleteRoleParam.getRoleId());
        return JsonResponse.success();
    }

    /**
     * 更新角色
     *
     * @param updateRoleParam 更新角色参数
     * @return 响应
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse updateRole(@Validated UpdateRoleParam updateRoleParam) {
        iRoleService.updateRole(updateRoleParam.getRoleId(), updateRoleParam.getRoleName(), updateRoleParam.getNote(), updateRoleParam.getPermissionIds());
        return JsonResponse.success();
    }


    /**
     * 检查roleName
     *
     * @param roleName 角色名
     */
    public void checkRoleName(String roleName) {
        String rolePrefix = "ROLE_";
        if (!roleName.contains(rolePrefix)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "角色名必须已ROLE_开头");
        }
        int maxlength = 11;
        if (roleName.length() > maxlength) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "角色名长度不能大于10");
        }
    }
}
