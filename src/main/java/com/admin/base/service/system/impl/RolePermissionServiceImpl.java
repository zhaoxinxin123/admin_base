package com.admin.base.service.system.impl;

import com.admin.base.component.EntityInit;
import com.admin.base.constant.ResponseCode;
import com.admin.base.constant.YesOrNo;
import com.admin.base.entity.system.Permissions;
import com.admin.base.entity.system.Role;
import com.admin.base.entity.system.RolePermission;
import com.admin.base.exception.BusinessException;
import com.admin.base.repository.system.PermissionsRepository;
import com.admin.base.repository.system.RolePermissionRepository;
import com.admin.base.service.system.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements IRolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionsRepository permissionsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleId(Integer roleId) {
        rolePermissionRepository.deleteByRoleId(toLongId(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermission(Integer roleId, List<Integer> newPermissionIds) {
        Long roleIdValue = toLongId(roleId);
        List<Long> oldPermissionIds = selectPermissionIdByRoleId(roleIdValue);
        List<Long> requestedPermissionIds = toLongIds(newPermissionIds);
        if (!oldPermissionIds.isEmpty()) {
            List<Long> pubIds = oldPermissionIds.stream().filter(requestedPermissionIds::contains).toList();
            List<Long> addIds = requestedPermissionIds.stream().filter(id -> !pubIds.contains(id)).toList();
            List<Long> deleteIds = oldPermissionIds.stream().filter(id -> !pubIds.contains(id)).toList();
            if (!deleteIds.isEmpty()) {
                rolePermissionRepository.deleteByRoleIdAndPermissionIdIn(roleIdValue, deleteIds);
            }
            saveRolePermissions(roleId, addIds);
        } else {
            saveRolePermissions(roleId, requestedPermissionIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertRolePermission(Integer roleId, List<Integer> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "请赋予角色一定权限！");
        }
        saveRolePermissions(roleId, toLongIds(permissionIds));
    }

    @Override
    public List<Long> selectPermissionIdByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();
    }

    @Override
    public List<Permissions> selectPermissionByRoles(List<Role> roles) {
        List<Long> roleIds = roles.stream().map(Role::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> permissionIds = rolePermissionRepository.findByRoleIdIn(roleIds)
                .stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionsRepository.findByStateAndPermissionIdIn(YesOrNo.YES, permissionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPermissionId(Integer permissionId) {
        rolePermissionRepository.deleteByPermissionId(toLongId(permissionId));
    }

    private void saveRolePermissions(Integer roleId, Collection<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = EntityInit.initRolePermission(roleId, permissionId.intValue());
            rolePermissionRepository.save(rolePermission);
        }
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }

    private List<Long> toLongIds(List<Integer> ids) {
        return ids == null ? List.of() : ids.stream().map(Integer::longValue).toList();
    }
}
