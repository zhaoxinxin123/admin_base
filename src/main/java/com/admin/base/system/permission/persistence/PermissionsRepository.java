package com.admin.base.system.permission.persistence;

import com.admin.base.system.permission.domain.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    List<Permissions> findByState(Integer state);

    List<Permissions> findByStateAndPermissionIdIn(Integer state, Collection<Long> permissionIds);

    List<Permissions> findByParentId(Long parentId);
}
