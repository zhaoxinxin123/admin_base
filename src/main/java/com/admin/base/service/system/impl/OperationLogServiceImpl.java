package com.admin.base.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.admin.base.dto.request.system.OperationLogListParam;
import com.admin.base.entity.system.OperationLog;
import com.admin.base.mapper.SysOperationLogMapper;
import com.admin.base.service.system.IOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.base.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * tb_sys_operation_log 服务实现类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, OperationLog> implements IOperationLogService {

    @Override
    public void insertOperationLog(OperationLog operationLog) {
        this.baseMapper.insert(operationLog);
    }

    @Override
    public IPage<OperationLog> listPage(OperationLogListParam operationListParam) {
        IPage<OperationLog> iPage = new Page<>(operationListParam.getPage(), operationListParam.getSize());
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(!StringUtils.isEmpty(operationListParam.getOperationName()), OperationLog::getOperationName, operationListParam.getOperationName())
                .eq(operationListParam.getOperationType() != null, OperationLog::getBusinessType, operationListParam.getOperationType())
                .eq(!StringUtils.isEmpty(operationListParam.getModelName()), OperationLog::getTitle, operationListParam.getModelName())
                .gt(operationListParam.getStartTime() != null, OperationLog::getOperationTime, operationListParam.getStartTime())
                .lt(operationListParam.getEndTime() != null, OperationLog::getOperationTime, operationListParam.getStartTime())
                .orderByDesc(OperationLog::getOperationTime);
        return this.baseMapper.selectPage(iPage, queryWrapper);
    }

    @Override
    public void deleteByIds(List<Integer> logIds) {

        if (logIds.size() > 0) {
            this.baseMapper.deleteBatchIds(logIds);
        }

    }
}
