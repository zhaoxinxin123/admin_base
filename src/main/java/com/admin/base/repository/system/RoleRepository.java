package com.admin.base.repository.system;

import com.admin.base.entity.system.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleName(String roleName);

    Optional<Role> findByRoleName(String roleName);
}
