package com.admin.base.system.role.persistence;

import com.admin.base.system.role.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleName(String roleName);

    Optional<Role> findByRoleName(String roleName);
}
