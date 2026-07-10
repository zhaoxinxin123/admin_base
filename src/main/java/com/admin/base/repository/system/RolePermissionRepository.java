package com.admin.base.repository.system;

import com.admin.base.entity.system.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByRoleIdIn(Collection<Long> roleIds);

    void deleteByRoleId(Long roleId);

    void deleteByRoleIdAndPermissionIdIn(Long roleId, Collection<Long> permissionIds);

    void deleteByPermissionId(Long permissionId);
}
