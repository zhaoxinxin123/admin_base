package com.admin.base.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.admin.base.component.EntityInit;
import com.admin.base.constant.ResponseCode;
import com.admin.base.constant.YesOrNo;
import com.admin.base.dto.request.system.UpdatePermissionParam;
import com.admin.base.dto.request.system.AddPermissionParam;
import com.admin.base.entity.system.Permissions;
import com.admin.base.exception.BusinessException;
import com.admin.base.mapper.system.PermissionsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.base.service.system.IPermissionsService;
import com.admin.base.service.system.IRolePermissionService;
import com.admin.base.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions> implements IPermissionsService {
    @Resource
    private IRolePermissionService iRolePermissionService;

    @Override
    public List<Permissions> getPermissionsByIds(List<Integer> ids) {
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Permissions::getState, YesOrNo.YES).in(!ids.isEmpty(), Permissions::getPermissionId, ids);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<Permissions> getAll() {
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Permissions::getState, YesOrNo.YES);
        return this.baseMapper.selectList(queryWrapper);
    }


    @Override
    public List<Permissions> getAllContainDisable() {
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AddPermissionParam addPermissionParam) {
        //检查parentId的level
        int level = checkLevel(addPermissionParam.getParentId());
        Permissions permissions = EntityInit.initPermission(addPermissionParam);
        permissions.setLevel(level);
        this.baseMapper.insert(permissions);
        //添加管理员权限
        iRolePermissionService.insertRolePermission(1, Collections.singletonList(permissions.getPermissionId()));
    }

    @Override
    public void updatePermission(UpdatePermissionParam updatePermissionParam) {
        Permissions currentPermission = this.baseMapper.selectById(updatePermissionParam.getPermissionId());
        if (currentPermission == null) {
            throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "不存在该权限记录");
        }
        //检查parentId的level
        int level = checkLevel(updatePermissionParam.getParentId());
        UpdateWrapper<Permissions> updateWrapper = new UpdateWrapper<>();
        //icon标记
        boolean iconFlag = !StringUtils.isEmpty(currentPermission.getUrl()) && !currentPermission.getUrl().equals(updatePermissionParam.getIcon());
        if (StringUtils.isEmpty(currentPermission.getUrl()) && !StringUtils.isEmpty(updatePermissionParam.getIcon())) {
            iconFlag = true;
        }

        updateWrapper.lambda().eq(Permissions::getPermissionId, updatePermissionParam.getPermissionId())
                .set(!currentPermission.getParentId().equals(updatePermissionParam.getParentId()), Permissions::getParentId, updatePermissionParam.getParentId())
                .set(Permissions::getPerm, updatePermissionParam.getPerm())
                .set(Permissions::getLevel, level)
                .set(iconFlag, Permissions::getUrl, updatePermissionParam.getIcon())
                .set(Permissions::getTitle, updatePermissionParam.getTitle())
                .set(Permissions::getPath, updatePermissionParam.getPath())
                .set(!currentPermission.getState().equals(updatePermissionParam.getState()), Permissions::getState, updatePermissionParam.getState())
                .set(Permissions::getUpdateTime, LocalDateTime.now());
        this.baseMapper.update(null, updateWrapper);

    }

    private int checkLevel(Integer parentId) {
        int level = 0;
        if (!parentId.equals(0)) {
            final Permissions parentPermission = this.baseMapper.selectById(parentId);
            if (parentPermission == null || parentPermission.getLevel() > 1) {
                throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "选择正确的父级id");
            }
            level = parentPermission.getLevel() + 1;
        }
        return level;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer permissionId) {
        Permissions currentPermission = this.baseMapper.selectById(permissionId);
        if (currentPermission == null) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "菜单已被删除");
        }
        this.baseMapper.deleteById(permissionId);
        //删除角色权限中所有该值
        iRolePermissionService.deleteByPermissionId(permissionId);

        //删除该id下的所有子菜单
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Permissions::getParentId, permissionId);
        final List<Permissions> permissions = this.baseMapper.selectList(queryWrapper);
        for (Permissions permission : permissions) {
            this.baseMapper.deleteById(permission.getPermissionId());
            iRolePermissionService.deleteByPermissionId(permission.getPermissionId());
        }

    }
}
