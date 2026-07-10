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

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_sys_global_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sys_global_config_key", columnNames = "config_key")
})
public class GlobalConfig extends AuditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "config_value", nullable = false, length = 512)
    private String configValue;

    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    @Column(name = "note")
    private String note;
}
