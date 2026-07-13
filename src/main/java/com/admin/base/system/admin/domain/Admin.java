package com.admin.base.system.admin.domain;

import com.admin.base.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统管理员表。
 *
 * <p>{@link Entity} 声明这是 JPA 实体；{@link Table} 绑定真实表名、索引和唯一约束。
 * 当前项目使用 ddl-auto=validate，不通过实体自动建表；这里显式写出
 * {@link Column} 是为了让字段名、长度、非空约束和 MySQL 类型与 v2 schema
 * 保持一致。JPA 也支持按属性名自动映射，但遇到 user_name 这类下划线列名、
 * TINYINT 类型或长度约束时，显式声明更清楚也更安全。</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "tb_sys_admin", indexes = {
        @Index(name = "idx_sys_admin_state", columnList = "state")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_admin_user_name", columnNames = "user_name")
})
public class Admin extends AuditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 管理员主键。IDENTITY 表示使用 MySQL AUTO_INCREMENT 生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    /**
     * 昵称。
     */
    @Column(name = "nickname", nullable = false, length = 64)
    private String nickname;

    /**
     * 登录名，对应唯一索引 uk_sys_admin_user_name。
     */
    @Column(name = "user_name", nullable = false, length = 64)
    private String userName;

    /**
     * BCrypt 加密后的密码。
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 账号状态。显式 TINYINT 避免 Hibernate 将 Integer 与 MySQL BIT/TINYINT(1) 误匹配。
     */
    @Column(name = "state", nullable = false, columnDefinition = "TINYINT")
    private Integer state;

    /**
     * 仅用于接口返回/初始化展示，不持久化到数据库。
     */
    @Transient
    private String passwordShow;
}
