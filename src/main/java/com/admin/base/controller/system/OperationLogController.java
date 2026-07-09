package com.admin.base.controller.system;


import com.admin.base.common.PageResult;
import com.admin.base.dto.request.system.DeleteOperationLogParam;
import com.admin.base.dto.request.system.OperationLogListParam;
import com.admin.base.annotation.Log;
import com.admin.base.common.JsonResponse;
import com.admin.base.constant.log.BusinessType;
import com.admin.base.controller.common.BaseController;
import com.admin.base.entity.system.OperationLog;
import com.admin.base.service.system.IOperationLogService;
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

