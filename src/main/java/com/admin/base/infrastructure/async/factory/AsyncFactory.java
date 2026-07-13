package com.admin.base.infrastructure.async.factory;

import com.admin.base.system.log.entity.OperationLog;
import com.admin.base.system.log.service.IOperationLogService;
import com.admin.base.shared.util.spring.SpringUtils;

import java.util.TimerTask;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 9:13 上午
 * @desc 异步工厂 产生任务用
 */
public class AsyncFactory {
    /**
     * 操作日志记录
     *
     * @param operationLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOperation(final OperationLog operationLog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点
                SpringUtils.getBean(IOperationLogService.class).insertOperationLog(operationLog);
            }
        };
    }
}
