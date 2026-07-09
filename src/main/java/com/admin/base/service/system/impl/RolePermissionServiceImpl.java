package com.admin.base.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.admin.base.component.EntityInit;
import com.admin.base.constant.ResponseCode;
import com.admin.base.entity.system.Permissions;
import com.admin.base.entity.system.Role;
import com.admin.base.entity.system.RolePermission;
import com.admin.base.exception.BusinessException;
import com.admin.base.mapper.system.RolePermissionMapper;
import com.admin.base.service.system.IRolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements IRolePermissionService {

    @Override
    public void deleteByRoleId(Integer roleId) {
        QueryWrapper<RolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RolePermission::getRoleId, roleId);
        this.baseMapper.delete(queryWrapper);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermission(Integer roleId, List<Integer> newPermissionIds) {
        //查出角色已有权限ids
        final List<Integer> oldPermissionIds = selectPermissionIdByRoleId(roleId);
        if (oldPermissionIds.size()!=0){
            //两者取交集获得  共有的ids   -pubIds
            final List<Integer> pubIds = oldPermissionIds.stream().filter(newPermissionIds::contains).collect(Collectors.toList());
            //拿插入的newPermissionIds-pubIds    =需要插入的权限列表
            final List<Integer> addIds = newPermissionIds.stream().filter(id -> !pubIds.contains(id)).collect(Collectors.toList());
            //oldPermissionIds - pubIds    = 需要删除的权限列表
            final List<Integer> deleteIds = oldPermissionIds.stream().filter(id -> !pubIds.contains(id)).collect(Collectors.toList());
            //删除多余的权限
            if (deleteIds.size() > 0) {

                QueryWrapper<RolePermission> queryWrapper = new QueryWrapper<>();
                queryWrapper
                        .lambda()
                        .eq(RolePermission::getRoleId, roleId)
                        .in(RolePermission::getPermissionId, deleteIds);

                this.baseMapper.delete(queryWrapper);
            }
            //添加新权限
            if (addIds.size() > 0) {
                for (Integer addId : addIds) {
                    RolePermission rolePermission = EntityInit.initRolePermission(roleId, addId);
                    this.baseMapper.insert(rolePermission);
                }

            }
        }else {
            for (Integer newPermissionId : newPermissionIds) {
                RolePermission rolePermission = EntityInit.initRolePermission(roleId, newPermissionId);
                this.baseMapper.insert(rolePermission);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertRolePermission(Integer roleId, List<Integer> permissionIds) {
        if (permissionIds.size() == 0) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "请赋予角色一定权限！");
        }
        for (Integer permissionId : permissionIds) {
            RolePermission rolePermission = EntityInit.initRolePermission(roleId, permissionId);
            this.baseMapper.insert(rolePermission);
        }
    }

    @Override
    public List<Integer> selectPermissionIdByRoleId(Integer roleId) {
        return this.baseMapper.selectPermissionIdByRoleId(roleId);
    }

    @Override
    public List<Permissions> selectPermissionByRoles(List<Role> roles) {
        final List<Integer> roleIds = roles.stream().map(Role::getRoleId).collect(Collectors.toList());
        return this.baseMapper.selectPermissionByRoles(roleIds);
    }

    @Override
    public void deleteByPermissionId(Integer permissionId) {
        QueryWrapper<RolePermission> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(RolePermission::getPermissionId,permissionId);
        this.baseMapper.delete(queryWrapper);
    }


}
