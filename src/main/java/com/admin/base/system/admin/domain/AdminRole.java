package com.admin.base.system.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员与角色关系表。
 *
 * <p>{@link Entity} 标识 JPA 实体；{@link EntityListeners} 启用 Spring Data JPA
 * 审计能力，让 {@link CreatedDate} 自动写入 create_time。{@link Table} 中的唯一约束
 * 防止同一管理员重复绑定同一角色。</p>
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_sys_admin_role", indexes = {
        @Index(name = "idx_sys_admin_role_role_id", columnList = "role_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_admin_role_admin_role", columnNames = {"admin_id", "role_id"})
})
public class AdminRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}
