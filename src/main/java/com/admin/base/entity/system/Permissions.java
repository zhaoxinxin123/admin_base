package com.admin.base.entity.system;

import com.admin.base.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_sys_permissions", indexes = {
        @Index(name = "idx_sys_permissions_parent_id", columnList = "parent_id"),
        @Index(name = "idx_sys_permissions_state", columnList = "state")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_permissions_perm", columnNames = "perm")
})
public class Permissions extends AuditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "level", nullable = false, columnDefinition = "TINYINT")
    private Integer level;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "perm", nullable = false, length = 128)
    private String perm;

    @Column(name = "require_auth", nullable = false, columnDefinition = "TINYINT")
    private Integer requireAuth;

    @Column(name = "state", nullable = false, columnDefinition = "TINYINT")
    private Integer state;

    @Column(name = "url")
    private String url;

    @Column(name = "title", nullable = false, length = 64)
    private String title;
}
