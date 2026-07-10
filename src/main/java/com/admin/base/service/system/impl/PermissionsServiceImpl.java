package com.admin.base.service.system.impl;

import com.admin.base.component.EntityInit;
import com.admin.base.constant.ResponseCode;
import com.admin.base.constant.YesOrNo;
import com.admin.base.dto.request.system.AddPermissionParam;
import com.admin.base.dto.request.system.UpdatePermissionParam;
import com.admin.base.entity.system.Permissions;
import com.admin.base.exception.BusinessException;
import com.admin.base.repository.system.PermissionsRepository;
import com.admin.base.service.system.IPermissionsService;
import com.admin.base.service.system.IRolePermissionService;
import com.admin.base.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

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
        int level = checkLevel(addPermissionParam.getParentId());
        Permissions permissions = EntityInit.initPermission(addPermissionParam);
        permissions.setLevel(level);
        Permissions saved = permissionsRepository.save(permissions);
        iRolePermissionService.insertRolePermission(1, Collections.singletonList(saved.getPermissionId().intValue()));
    }

    @Override
    public void updatePermission(UpdatePermissionParam updatePermissionParam) {
        Permissions currentPermission = permissionsRepository.findById(toLongId(updatePermissionParam.getPermissionId()))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "不存在该权限记录"));
        int level = checkLevel(updatePermissionParam.getParentId());
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

    private int checkLevel(Integer parentId) {
        int level = 0;
        if (!parentId.equals(0)) {
            Permissions parentPermission = permissionsRepository.findById(toLongId(parentId))
                    .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "选择正确的父级id"));
            if (parentPermission.getLevel() > 1) {
                throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "选择正确的父级id");
            }
            level = parentPermission.getLevel() + 1;
        }
        return level;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer permissionId) {
        Permissions currentPermission = permissionsRepository.findById(toLongId(permissionId))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_ALERT, "菜单已被删除"));
        permissionsRepository.delete(currentPermission);
        iRolePermissionService.deleteByPermissionId(permissionId);

        List<Permissions> permissions = permissionsRepository.findByParentId(toLongId(permissionId));
        for (Permissions permission : permissions) {
            permissionsRepository.deleteById(permission.getPermissionId());
            iRolePermissionService.deleteByPermissionId(permission.getPermissionId().intValue());
        }
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }
}
