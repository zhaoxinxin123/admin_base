package com.admin.base.system.permission.service.impl;

import com.admin.base.shared.factory.EntityFactory;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.shared.constant.YesOrNo;
import com.admin.base.system.permission.dto.AddPermissionParam;
import com.admin.base.system.permission.dto.UpdatePermissionParam;
import com.admin.base.system.permission.entity.Permissions;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.permission.repository.PermissionsRepository;
import com.admin.base.system.permission.service.IPermissionsService;
import com.admin.base.system.role.service.IRolePermissionService;
import com.admin.base.shared.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionsServiceImpl implements IPermissionsService {

    private final PermissionsRepository permissionsRepository;
    private final IRolePermissionService iRolePermissionService;

    @Override
    public List<Permissions> getPermissionsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return permissionsRepository.findByStateAndPermissionIdIn(YesOrNo.YES, ids.stream().map(Integer::longValue).toList());
    }

    @Override
    public List<Permissions> getAll() {
        return permissionsRepository.findByState(YesOrNo.YES);
    }

    @Override
    public List<Permissions> getAllContainDisable() {
        return permissionsRepository.findAll(Sort.by(Sort.Direction.ASC, "permissionId"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AddPermissionParam addPermissionParam) {
        int level = checkLevel(addPermissionParam.getParentId(), null);
        Permissions permissions = EntityFactory.initPermission(addPermissionParam);
        permissions.setLevel(level);
        Permissions saved = permissionsRepository.save(permissions);
        iRolePermissionService.insertRolePermission(1, Collections.singletonList(saved.getPermissionId().intValue()));
    }

    @Override
    public void updatePermission(UpdatePermissionParam updatePermissionParam) {
        Permissions currentPermission = permissionsRepository.findById(toLongId(updatePermissionParam.getPermissionId()))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "不存在该权限记录"));
        int level = checkLevel(updatePermissionParam.getParentId(), currentPermission.getPermissionId());
        boolean iconFlag = !StringUtils.isEmpty(currentPermission.getUrl()) && !currentPermission.getUrl().equals(updatePermissionParam.getIcon());
        if (StringUtils.isEmpty(currentPermission.getUrl()) && !StringUtils.isEmpty(updatePermissionParam.getIcon())) {
            iconFlag = true;
        }

        currentPermission.setParentId(toLongId(updatePermissionParam.getParentId()));
        currentPermission.setPerm(updatePermissionParam.getPerm());
        currentPermission.setLevel(level);
        if (iconFlag) {
            currentPermission.setUrl(updatePermissionParam.getIcon());
        }
        currentPermission.setTitle(updatePermissionParam.getTitle());
        currentPermission.setPath(updatePermissionParam.getPath());
        currentPermission.setState(updatePermissionParam.getState());
        permissionsRepository.save(currentPermission);
    }

    private int checkLevel(Integer parentId, Long permissionId) {
        int level = 0;
        if (!parentId.equals(0)) {
            Permissions parentPermission = permissionsRepository.findById(toLongId(parentId))
                    .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "选择正确的父级id"));
            if (parentPermission.getLevel() > 1) {
                throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "选择正确的父级id");
            }
            ensureNoParentCycle(permissionId, parentPermission);
            level = parentPermission.getLevel() + 1;
        }
        return level;
    }

    private void ensureNoParentCycle(Long permissionId, Permissions parentPermission) {
        if (permissionId == null) {
            return;
        }
        Set<Long> visited = new HashSet<>();
        Permissions current = parentPermission;
        while (current != null && current.getPermissionId() != null) {
            if (permissionId.equals(current.getPermissionId()) || !visited.add(current.getPermissionId())) {
                throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "不能选择当前权限或其子权限作为父级");
            }
            Long parentId = current.getParentId();
            if (parentId == null || parentId == 0) {
                return;
            }
            current = permissionsRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "选择正确的父级id"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer permissionId) {
        Permissions currentPermission = permissionsRepository.findById(toLongId(permissionId))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_ALERT, "菜单已被删除"));
        Set<Long> subtreeIds = new LinkedHashSet<>();
        collectSubtreeIds(currentPermission, subtreeIds);
        for (Long subtreeId : subtreeIds) {
            iRolePermissionService.deleteByPermissionId(subtreeId.intValue());
        }
        permissionsRepository.deleteAllById(List.copyOf(subtreeIds));
    }

    private void collectSubtreeIds(Permissions permission, Set<Long> subtreeIds) {
        if (!subtreeIds.add(permission.getPermissionId())) {
            return;
        }
        for (Permissions child : permissionsRepository.findByParentId(permission.getPermissionId())) {
            collectSubtreeIds(child, subtreeIds);
        }
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }
}
