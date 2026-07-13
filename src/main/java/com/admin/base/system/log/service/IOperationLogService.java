package com.admin.base.system.log.service;

import com.admin.base.shared.api.PageResult;
import com.admin.base.system.log.dto.OperationLogListParam;
import com.admin.base.system.log.entity.OperationLog;

import java.util.List;

/**
 * <p>
 * tb_sys_operation_log 服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
public interface IOperationLogService {
    /**
     * 插入日志记录
     * @param operationLog  日志
     */
    void insertOperationLog(OperationLog operationLog);

    /**
     * 分页查询
     * @param operationListParam
     * @return
     */
    PageResult<OperationLog> listPage(OperationLogListParam operationListParam);

    /**
     * 删除日志信息
     * @param logIds  日志id列表
     */
    void deleteByIds(List<Integer> logIds);
}
