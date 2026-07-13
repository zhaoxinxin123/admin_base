package com.admin.base.system.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表。
 *
 * <p>{@link Entity} 标识 JPA 实体；{@link Table} 绑定表名并声明日志列表页常用筛选索引。
 * JSON、TEXT、TINYINT 等 MySQL 特定类型通过 {@link Column#columnDefinition()} 明确，
 * 避免 Hibernate validate 与真实表结构出现类型歧义。</p>
 */
@Data
@Entity
@Table(name = "tb_sys_operation_log", indexes = {
        @Index(name = "idx_sys_operation_log_operation_name", columnList = "operation_name"),
        @Index(name = "idx_sys_operation_log_business_type", columnList = "business_type"),
        @Index(name = "idx_sys_operation_log_operation_time", columnList = "operation_time"),
        @Index(name = "idx_sys_operation_log_success", columnList = "success")
})
public class OperationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Long operationId;

    @Column(name = "title", length = 64)
    private String title;

    @Column(name = "business_type", columnDefinition = "TINYINT")
    private Integer businessType;

    @Column(name = "method")
    private String method;

    @Column(name = "request_method", length = 16)
    private String requestMethod;

    @Column(name = "operation_name", length = 64)
    private String operationName;

    @Column(name = "operation_url")
    private String operationUrl;

    @Column(name = "operation_ip", length = 64)
    private String operationIp;

    @Column(name = "operation_param", columnDefinition = "json")
    private String operationParam;

    @Column(name = "json_result", columnDefinition = "json")
    private String jsonResult;

    @Column(name = "success", nullable = false, columnDefinition = "TINYINT")
    private Integer success = 1;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    public Integer getStatus() {
        return statusCode;
    }

    public void setStatus(Integer status) {
        this.statusCode = status;
        this.success = Integer.valueOf(200).equals(status) ? 1 : 0;
    }
}
