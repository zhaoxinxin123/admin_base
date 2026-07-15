package com.admin.base.system.role.service.impl;

import com.admin.base.shared.api.PageResult;
import com.admin.base.shared.factory.EntityFactory;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.system.role.entity.Role;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.role.repository.RoleRepository;
import com.admin.base.system.role.service.IRolePermissionService;
import com.admin.base.system.role.service.IRoleService;
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

    private static final String ROLE_PREFIX = "ROLE_";
    private static final int ROLE_NAME_MAX_LENGTH = 10;

    private final RoleRepository roleRepository;
    private final IRolePermissionService iRolePermissionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addRole(String note, String roleName, List<Integer> permissionIds) {
        validateRoleName(roleName);
        if (checkExitsByRoleName(roleName)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该角色名称已存在");
        }
        Role role = roleRepository.save(EntityFactory.initRole(roleName, note));
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
        validateRoleName(roleName);
        Role role = roleRepository.findById(toLongId(roleId))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_ALERT, "角色不存在"));
        if (roleRepository.existsByRoleNameAndRoleIdNot(roleName, role.getRoleId())) {
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

    private void validateRoleName(String roleName) {
        if (roleName == null || !roleName.startsWith(ROLE_PREFIX)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "角色名必须以ROLE_开头");
        }
        if (roleName.length() > ROLE_NAME_MAX_LENGTH) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "角色名长度不能大于10");
        }
    }
}
