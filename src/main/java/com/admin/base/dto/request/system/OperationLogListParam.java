package com.admin.base.dto.request.system;

import com.admin.base.dto.request.common.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 2:06 下午
 * @desc  操作日志列表参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogListParam extends PageParam {
    /**
     * 操作类型
     */
    private  Integer operationType;

    /**
     * 操作人名称
     */
    private String operationName;
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    /**
     * 模块名
     */
    private String modelName;

}
