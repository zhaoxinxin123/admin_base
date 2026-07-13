package com.admin.base.system.config.web;


import com.admin.base.shared.api.PageResult;
import com.admin.base.shared.constant.log.BusinessType;
import com.admin.base.system.config.dto.AddGlobalConfigParam;
import com.admin.base.system.config.dto.DeleteGlobalConfigParam;
import com.admin.base.system.config.dto.UpdateGlobalConfigParam;
import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.system.config.dto.ListGlobalConfigParam;
import com.admin.base.system.config.domain.GlobalConfig;
import com.admin.base.system.config.application.IGlobalConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.admin.base.infrastructure.web.BaseController;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * <p>
 * tb_global_config 前端控制器
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
@RestController
@RequestMapping("/sys_global_config")
public class GlobalConfigController extends BaseController {
    @Resource
    private IGlobalConfigService iGlobalConfigService;

    @PreAuthorize("hasAuthority('sys:configList')")
    @PostMapping("/list")
    public JsonResponse list(@Validated ListGlobalConfigParam listGlobalConfigParam) {
        PageResult<GlobalConfig> pageResult = iGlobalConfigService.selectByPage(listGlobalConfigParam.getPage(), listGlobalConfigParam.getSize(), listGlobalConfigParam.getKey(), listGlobalConfigParam.getNote());
        final Map<String, Object> dataTable = getDataTable(pageResult, 6);
        return JsonResponse.success(dataTable);
    }

    @Log(title = "sys", businessType = BusinessType.INSERT, isSaveRequestData = false)
    @PreAuthorize("hasAuthority('sys:setting:add')")
    @PostMapping("/add")
    public JsonResponse add(@Validated AddGlobalConfigParam addGlobalConfigParam) {
        iGlobalConfigService.add(addGlobalConfigParam);
        return JsonResponse.success();
    }


    @Log(title = "sys", businessType = BusinessType.DELETE, isSaveRequestData = false)
    @PreAuthorize("hasAuthority('sys:setting:delete')")
    @PostMapping("/deleteBatch")
    public JsonResponse deleteBatch(@Validated DeleteGlobalConfigParam deleteGlobalConfigParam) {
        iGlobalConfigService.deleteByIds(deleteGlobalConfigParam.getConfigIds());
        return JsonResponse.success();
    }

    @Log(title = "sys", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    @PreAuthorize("hasAuthority('sys:setting:update')")
    @PostMapping("/update")
    public JsonResponse deleteBatch(@Validated UpdateGlobalConfigParam updateGlobalConfigParam) {
        iGlobalConfigService.updateConfig(updateGlobalConfigParam);
        return JsonResponse.success();
    }

}
