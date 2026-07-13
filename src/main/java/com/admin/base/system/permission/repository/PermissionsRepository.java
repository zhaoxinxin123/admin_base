package com.admin.base.system.permission.repository;

import com.admin.base.system.permission.entity.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    List<Permissions> findByState(Integer state);

    List<Permissions> findByStateAndPermissionIdIn(Integer state, Collection<Long> permissionIds);

    List<Permissions> findByParentId(Long parentId);
}
