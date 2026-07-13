package com.admin.base.system.admin.application.impl;

import com.admin.base.shared.factory.EntityFactory;
import com.admin.base.system.admin.domain.Admin;
import com.admin.base.system.admin.domain.AdminRole;
import com.admin.base.system.role.domain.Role;
import com.admin.base.system.admin.persistence.AdminRoleRepository;
import com.admin.base.system.role.persistence.RoleRepository;
import com.admin.base.system.admin.application.IAdminRoleService;
import com.admin.base.system.admin.application.IAdminService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员角色关系服务。
 *
 * <p>迁移到 JPA 后不再继承 MyBatis Plus ServiceImpl，所有持久化操作通过 Repository 完成。</p>
 */
@Service
public class AdminRoleServiceImpl implements IAdminRoleService {

    private final AdminRoleRepository adminRoleRepository;
    private final RoleRepository roleRepository;
    private final IAdminService iAdminService;

    public AdminRoleServiceImpl(
            AdminRoleRepository adminRoleRepository,
            RoleRepository roleRepository,
            // AdminServiceImpl 也依赖 IAdminRoleService；延迟注入用于打破这个既有循环依赖。
            @Lazy IAdminService iAdminService) {
        this.adminRoleRepository = adminRoleRepository;
        this.roleRepository = roleRepository;
        this.iAdminService = iAdminService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdminRole(Long adminId, Integer roleId) {
        Long roleIdValue = toLongId(roleId);
        // 关系表有 admin_id + role_id 唯一约束，保存前先做幂等判断。
        if (adminRoleRepository.existsByAdminIdAndRoleId(adminId, roleIdValue)) {
            return;
        }
        adminRoleRepository.save(EntityFactory.initAdminRole(adminId, roleId));
    }

    @Override
    public List<Role> selectByAdminId(Long adminId) {
        List<Long> roleIds = adminRoleRepository.findByAdminId(adminId)
                .stream()
                .map(AdminRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleRepository.findAllById(roleIds);
    }

    @Override
    public List<Role> selectRoleByName(String userName) {
        Admin admin = iAdminService.selectByUserName(userName);
        return selectByAdminId(admin.getAdminId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminOfRole(Integer adminId, List<Integer> roleIds) {
        Long adminIdValue = toLongId(adminId);
        List<Long> oldRoleIds = adminRoleRepository.findByAdminId(adminIdValue)
                .stream()
                .map(AdminRole::getRoleId)
                .toList();
        List<Long> requestedRoleIds = roleIds == null ? List.of() : roleIds.stream().map(Integer::longValue).toList();
        // 保留交集，只删除旧集合独有角色，只新增新集合独有角色。
        List<Long> pubIds = oldRoleIds.stream().filter(requestedRoleIds::contains).toList();
        List<Long> addIds = requestedRoleIds.stream().filter(id -> !pubIds.contains(id)).toList();
        List<Long> deleteIds = oldRoleIds.stream().filter(id -> !pubIds.contains(id)).toList();
        for (Long deleteId : deleteIds) {
            adminRoleRepository.deleteByAdminIdAndRoleId(adminIdValue, deleteId);
        }
        for (Long roleId : addIds) {
            addAdminRole(adminIdValue, roleId.intValue());
        }
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }
}
