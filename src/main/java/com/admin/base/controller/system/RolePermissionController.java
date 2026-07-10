package com.admin.base.controller.system;


import com.admin.base.common.PageResult;
import com.admin.base.common.JsonResponse;
import com.admin.base.controller.common.BaseController;
import com.admin.base.dto.request.common.PageParam;
import com.admin.base.dto.response.system.PermissionResponse;
import com.admin.base.dto.response.system.RoleResponse;
import com.admin.base.entity.system.Permissions;
import com.admin.base.entity.system.Role;
import com.admin.base.service.system.IPermissionsService;
import com.admin.base.service.system.IRolePermissionService;
import com.admin.base.service.system.IRoleService;
import com.admin.base.utils.ListEntityConvert;
import com.admin.base.utils.MenuUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@RestController
@RequestMapping("/role_permission")
public class RolePermissionController extends BaseController {
    @Resource
    private IRoleService iRoleService;

    @Resource
    private IPermissionsService iPermissionsService;

    @Resource
    private IRolePermissionService iRolePermissionService;


    /**
     * 角色列表
     */
    @PostMapping("/manageList")
    @PreAuthorize("hasAuthority('sys:roleList')")
    public JsonResponse listRole(@Validated PageParam pageParam) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final PageResult<Role> rolePage = iRoleService.getRolePage(pageParam.getPage(), pageParam.getSize());
        final Map<String, Object> dataTable = getDataTable(rolePage, 6);
        final List<Permissions> all = iPermissionsService.getAll();
        //构造返回结果列表
        List<RoleResponse> roleResponses = new ArrayList<>();
        //封装返回结果
        for (Role record : rolePage.rows()) {
            RoleResponse roleResponse = new RoleResponse();
            //获取权限树
            final List<PermissionResponse> permissionResponses = ListEntityConvert.listCopyToAnotherList(PermissionResponse.class, all);
            //获取角色所有权限的ids
            final List<Long> list = iRolePermissionService.selectPermissionIdByRoleId(record.getRoleId());
            assert permissionResponses != null;
            List<PermissionResponse> rootMenus = permissionResponses.stream()
                    .filter(item -> item.getLevel().equals(0))
                    .collect(Collectors.toList());
            rootMenus.forEach(rootMenu ->
                    rootMenu.setChild(MenuUtils.getChild(permissionResponses, rootMenu.getPermissionId(), rootMenu)));
            BeanUtils.copyProperties(record, roleResponse);
            //检查该管理员下 是否选中 list
            List<PermissionResponse> resultPermissionResponses = MenuUtils.setSelected(rootMenus, list);
            roleResponse.setPermissionResponses(resultPermissionResponses);
            roleResponses.add(roleResponse);
        }
        dataTable.put("rows", roleResponses);
        return JsonResponse.success(dataTable);
    }

}
