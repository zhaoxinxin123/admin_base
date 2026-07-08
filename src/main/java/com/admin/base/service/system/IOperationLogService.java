package com.admin.base.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.base.dto.request.system.OperationLogListParam;
import com.admin.base.entity.system.OperationLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * tb_sys_operation_log 服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
public interface IOperationLogService extends IService<OperationLog> {
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
    IPage<OperationLog> listPage(OperationLogListParam operationListParam);

    /**
     * 删除日志信息
     * @param logIds  日志id列表
     */
    void deleteByIds(List<Integer> logIds);
}
