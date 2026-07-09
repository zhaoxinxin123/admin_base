package com.admin.base.controller.system;


import com.admin.base.common.PageResult;
import com.admin.base.constant.AdminStatus;
import com.admin.base.constant.ResponseCode;
import com.admin.base.constant.log.BusinessType;
import com.admin.base.dto.request.system.UpdateAdminPasswordParam;
import com.admin.base.annotation.Log;
import com.admin.base.common.JsonResponse;
import com.admin.base.dto.request.system.ListAdminParam;
import com.admin.base.dto.request.system.UpdateAdminOfRoleParam;
import com.admin.base.dto.request.system.UpdateAdminStateParam;
import com.admin.base.dto.response.system.AdminResponse;
import com.admin.base.dto.response.system.RoleResponse;
import com.admin.base.entity.system.Admin;
import com.admin.base.entity.system.Role;
import com.admin.base.exception.BusinessException;
import com.admin.base.service.system.IAdminRoleService;
import com.admin.base.service.system.IAdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.admin.base.controller.common.BaseController;

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
    @PreAuthorize("hasAuthority('sys:adminList:resetPassword')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse updatePassword(@Validated UpdateAdminPasswordParam updateAdminPasswordParam) {
        //更新管理员密码
        iAdminService.updatePassword(updateAdminPasswordParam.getAdminId(), updateAdminPasswordParam.getPassword());
        return JsonResponse.success();
    }


    @PostMapping("/updateState")
    @PreAuthorize("hasAuthority('sys:adminList:diable')")
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
    @PreAuthorize("hasAuthority('sys:adminList:updateRole')")
    @Log(title = "sys", businessType = BusinessType.UPDATE)
    public JsonResponse updateAdminOfRole(@Validated UpdateAdminOfRoleParam updateAdminOfRoleParam) {
        iAdminRoleService.updateAdminOfRole(updateAdminOfRoleParam.getAdminId(), updateAdminOfRoleParam.getRoleIds());
        return JsonResponse.success();
    }



}

