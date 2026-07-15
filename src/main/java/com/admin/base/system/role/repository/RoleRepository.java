package com.admin.base.system.role.repository;

import com.admin.base.system.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleName(String roleName);

    boolean existsByRoleNameAndRoleIdNot(String roleName, Long roleId);

    Optional<Role> findByRoleName(String roleName);
}
