package com.admin.base.repository.system;

import com.admin.base.entity.system.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long>, JpaSpecificationExecutor<GlobalConfig> {
    Optional<GlobalConfig> findByConfigKey(String configKey);

    boolean existsByConfigKey(String configKey);
}
