package com.admin.base.system.admin.repository;

import com.admin.base.system.admin.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 管理员 JPA 仓储。
 *
 * <p>增删改和按 ID 查询等通用能力由 {@link JpaRepository} 隐式提供，例如
 * save、findById、findAll、deleteById、deleteAllById。这里只声明业务需要的
 * 用户名查询、存在性判断和列表模糊查询方法。</p>
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserName(String userName);

    boolean existsByUserName(String userName);

    Page<Admin> findByUserNameContaining(String userName, Pageable pageable);
}
