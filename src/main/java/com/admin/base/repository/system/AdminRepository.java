package com.admin.base.repository.system;

import com.admin.base.entity.system.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserName(String userName);

    boolean existsByUserName(String userName);

    Page<Admin> findByUserNameContaining(String userName, Pageable pageable);
}
