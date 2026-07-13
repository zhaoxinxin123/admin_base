package com.admin.base.system.role.application.impl;

import com.admin.base.shared.api.PageResult;
import com.admin.base.infrastructure.bootstrap.EntityInit;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.system.role.domain.Role;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.role.persistence.RoleRepository;
import com.admin.base.system.role.application.IRolePermissionService;
import com.admin.base.system.role.application.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository roleRepository;
    private final IRolePermissionService iRolePermissionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addRole(String note, String roleName, List<Integer> permissionIds) {
        if (checkExitsByRoleName(roleName)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该角色名称已存在");
        }
        Role role = roleRepository.save(EntityInit.initRole(roleName, note));
        iRolePermissionService.insertRolePermission(role.getRoleId().intValue(), permissionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer roleId) {
        iRolePermissionService.deleteByRoleId(roleId);
        roleRepository.deleteById(toLongId(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Integer roleId, String roleName, String note, List<Integer> permissionIds) {
        Role role = roleRepository.findById(toLongId(roleId))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_ALERT, "角色不存在"));
        if (checkExitsByRoleName(roleName) && !toLongId(roleId).equals(role.getRoleId())) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该角色名称已存在");
        }
        role.setRoleName(roleName);
        role.setNote(note);
        roleRepository.save(role);
        iRolePermissionService.updateRolePermission(roleId, permissionIds);
    }

    @Override
    public boolean checkExitsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }

    @Override
    public List<Role> getRoleList() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "roleId"));
    }

    @Override
    public PageResult<Role> getRolePage(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "roleId"));
        Page<Role> pageResult = roleRepository.findAll(pageable);
        return new PageResult<>(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }
}
