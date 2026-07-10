package com.admin.base.repository.system;

import com.admin.base.entity.system.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    List<Permissions> findByState(Integer state);

    List<Permissions> findByStateAndPermissionIdIn(Integer state, Collection<Long> permissionIds);

    List<Permissions> findByParentId(Long parentId);
}
