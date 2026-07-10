package com.admin.base.entity.system;

import com.admin.base.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统角色表。
 *
 * <p>{@link Entity} 标识 JPA 实体；{@link Table} 绑定 tb_sys_role 并声明角色名唯一约束。
 * 本项目只校验 schema，不根据实体自动建表；{@link Column} 用来记录和校验列名、长度及非空规则。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_sys_role", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_role_role_name", columnNames = "role_name")
})
public class Role extends AuditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, length = 64)
    private String roleName;

    @Column(name = "note")
    private String note;
}
