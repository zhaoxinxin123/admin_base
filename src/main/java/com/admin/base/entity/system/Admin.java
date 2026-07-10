package com.admin.base.entity.system;

import com.admin.base.entity.AuditableEntity;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "nickname", nullable = false, length = 64)
    private String nickname;

    @Column(name = "user_name", nullable = false, length = 64)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "state", nullable = false, columnDefinition = "TINYINT")
    private Integer state;

    @Transient
    private String passwordShow;
}
