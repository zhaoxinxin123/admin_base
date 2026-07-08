package com.admin.base.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.admin.base.component.EntityInit;
import com.admin.base.entity.system.Admin;
import com.admin.base.entity.system.AdminRole;
import com.admin.base.entity.system.Role;
import com.admin.base.mapper.system.AdminRoleMapper;
import com.admin.base.service.system.IAdminRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.base.service.system.IAdminService;
import com.admin.base.service.system.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class  AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements IAdminRoleService {
    @Lazy
    @Autowired
    private IAdminService iAdminService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdminRole(Integer adminId, Integer roleId) {
        AdminRole adminRole = EntityInit.initAdminRole(adminId, roleId);
        //如果adminId和RoleId 已存在 直接返回
        QueryWrapper<AdminRole> adminRoleQueryWrapper = new QueryWrapper<>();
        adminRoleQueryWrapper.lambda().eq(AdminRole::getAdminId, adminId)
                .eq(AdminRole::getRoleId, roleId);
        final Long count = this.baseMapper.selectCount(adminRoleQueryWrapper);
        if (count > 0) {
            return;
        }
        this.baseMapper.insert(adminRole);
    }

    @Override
    public List<Role> selectByAdminId(Integer adminId) {
        return this.baseMapper.selectRoleByAdminId(adminId);
    }

    @Override
    public List<Role> selectRoleByName(String userName) {
        final Admin admin = iAdminService.selectByUserName(userName);
        return selectByAdminId(admin.getAdminId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminOfRole(Integer adminId, List<Integer> roleIds) {
        final List<Role> oldRoles = selectByAdminId(adminId);
        if (oldRoles.size() != 0) {
            final List<Integer> oldRoleIds = oldRoles.stream().map(Role::getRoleId).collect(Collectors.toList());
            //两者取交集获得  共有的ids
            final List<Integer> pubIds = oldRoleIds.stream().filter(roleIds::contains).collect(Collectors.toList());
            //拿插入的newPermissionIds-pubIds    =需要插入的权限列表
            final List<Integer> addIds = roleIds.stream().sequential().filter(id -> !pubIds.contains(id)).collect(Collectors.toList());
            //oldRoleIds - pubIds    = 需要删除的权限列表
            final List<Integer> deleteIds = oldRoleIds.stream().filter(id -> !pubIds.contains(id)).collect(Collectors.toList());
            if (deleteIds.size() > 0) {
                for (Integer deleteId : deleteIds) {
                    QueryWrapper<AdminRole> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(AdminRole::getAdminId, adminId)
                            .eq(AdminRole::getRoleId, deleteId);
                    this.baseMapper.delete(queryWrapper);
                }
            }
            for (Integer roleId : addIds) {
                addAdminRole(adminId, roleId);
            }
        } else {
            for (Integer roleId : roleIds) {
                addAdminRole(adminId, roleId);
            }
        }
    }
}
