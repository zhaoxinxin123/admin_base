package com.admin.base.system.admin.persistence;

import com.admin.base.system.admin.domain.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    boolean existsByAdminIdAndRoleId(Long adminId, Long roleId);

    List<AdminRole> findByAdminId(Long adminId);

    List<AdminRole> findByAdminIdIn(Collection<Long> adminIds);

    void deleteByAdminIdAndRoleId(Long adminId, Long roleId);

    void deleteByAdminId(Long adminId);
}
