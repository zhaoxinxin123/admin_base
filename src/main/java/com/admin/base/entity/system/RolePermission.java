package com.admin.base.entity.system;

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
 * 角色与权限关系表。
 *
 * <p>{@link Entity} 标识 JPA 实体；{@link EntityListeners} 启用创建时间审计。
 * {@link Table} 声明常用查询索引和 role_id + permission_id 唯一约束。</p>
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_sys_role_permission", indexes = {
        @Index(name = "idx_sys_role_permission_permission_id", columnList = "permission_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_role_permission_role_permission", columnNames = {"role_id", "permission_id"})
})
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}
