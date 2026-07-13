package com.admin.base.system.admin.controller;


import com.admin.base.shared.api.PageResult;
import com.admin.base.shared.constant.AdminStatus;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.shared.constant.log.BusinessType;
import com.admin.base.system.admin.dto.UpdateAdminPasswordParam;
import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.system.admin.dto.ListAdminParam;
import com.admin.base.system.admin.dto.UpdateAdminOfRoleParam;
import com.admin.base.system.admin.dto.UpdateAdminStateParam;
import com.admin.base.system.admin.dto.AdminResponse;
import com.admin.base.system.role.dto.RoleResponse;
import com.admin.base.system.admin.entity.Admin;
import com.admin.base.system.role.entity.Role;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.admin.service.IAdminRoleService;
import com.admin.base.system.admin.service.IAdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.admin.base.infrastructure.controller.BaseController;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@RestController
@RequestMapping("/admin_role")
public class AdminRoleController extends BaseController {
    @Resource
    private IAdminService iAdminService;

    @Resource
    private IAdminRoleService iAdminRoleService;

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('sys:adminList')")
    public JsonResponse adminList(@RequestBody @Validated ListAdminParam listAdminParam) {
        PageResult<Admin> adminList = iAdminService.getAdminList(listAdminParam.getPage(), listAdminParam.getSize(), listAdminParam.getUserName());
        final Map<String, Object> dataTable = getDataTable(adminList, 6);
        List<AdminResponse> list = new ArrayList<>();
        for (Admin record : adminList.rows()) {
            AdminResponse adminResponse = new AdminResponse();
            BeanUtils.copyProperties(record, adminResponse);
            //查询管理员角色信息
            final List<Role> roles = iAdminRoleService.selectByAdminId(record.getAdminId());
            List<RoleResponse> roleResponses = new ArrayList<>();
            for (Role role : roles) {
                RoleResponse roleResponse = new RoleResponse();
                BeanUtils.copyProperties(role, roleResponse);
                roleResponses.add(roleResponse);

            }
            adminResponse.setRoles(roleResponses);
            list.add(adminResponse);
        }
        dataTable.put("rows", list);
        return JsonResponse.success(dataTable);
    }


    @PostMapping("/resetPassword")
    @PreAuthorize("hasAuthority('sys:admin:resetPwd')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse updatePassword(@Validated UpdateAdminPasswordParam updateAdminPasswordParam) {
        //更新管理员密码
        iAdminService.updatePassword(updateAdminPasswordParam.getAdminId(), updateAdminPasswordParam.getPassword());
        return JsonResponse.success();
    }


    @PostMapping("/updateState")
    @PreAuthorize("hasAuthority('sys:admin:edit')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse updateAdminState(@Validated UpdateAdminStateParam updateAdminStateParam) {
        if (AdminStatus.COMMON != updateAdminStateParam.getState() && AdminStatus.DISABLE != updateAdminStateParam.getState()) {
            throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "请确认参数");
        }
        //在下次登录时有生效 因为Jwt 是无法失效的
        iAdminService.updateAdminState(updateAdminStateParam.getAdminId(), updateAdminStateParam.getState());
        return JsonResponse.success();
    }


    @PostMapping("/updateAdminOfRole")
    @PreAuthorize("hasAuthority('sys:admin:edit')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse updateAdminOfRole(@Validated UpdateAdminOfRoleParam updateAdminOfRoleParam) {
        iAdminRoleService.updateAdminOfRole(updateAdminOfRoleParam.getAdminId(), updateAdminOfRoleParam.getRoleIds());
        return JsonResponse.success();
    }



}
