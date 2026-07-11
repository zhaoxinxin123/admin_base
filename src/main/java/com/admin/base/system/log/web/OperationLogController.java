package com.admin.base.system.log.web;


import com.admin.base.shared.api.PageResult;
import com.admin.base.system.log.dto.DeleteOperationLogParam;
import com.admin.base.system.log.dto.OperationLogListParam;
import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.shared.api.JsonResponse;
import com.admin.base.shared.constant.log.BusinessType;
import com.admin.base.infrastructure.web.BaseController;
import com.admin.base.system.log.domain.OperationLog;
import com.admin.base.system.log.application.IOperationLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * <p>
 * tb_sys_operation_log 前端控制器
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
@RestController
@RequestMapping("/sys_operation_log")
public class OperationLogController extends BaseController {

    @Resource
    private IOperationLogService iOperationLogService;
    @PostMapping("/list")
    public JsonResponse list(@Validated OperationLogListParam operationListParam) {
        PageResult<OperationLog> pageResult = iOperationLogService.listPage(operationListParam);
        final Map<String, Object> dataTable = getDataTable(pageResult, 6);
        return JsonResponse.success(dataTable);
    }

    @PostMapping("/deleteBatch")
    @Log(title = "sys",businessType = BusinessType.DELETE)
    public JsonResponse deleteByIds(DeleteOperationLogParam deleteOperationLogParam){
        iOperationLogService.deleteByIds(deleteOperationLogParam.getLogIds());
        return JsonResponse.success();
    }


}

