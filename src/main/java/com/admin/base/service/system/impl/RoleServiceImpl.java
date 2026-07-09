package com.admin.base.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.admin.base.common.PageResult;
import com.admin.base.component.EntityInit;
import com.admin.base.constant.ResponseCode;
import com.admin.base.exception.BusinessException;
import com.admin.base.mapper.system.RoleMapper;
import com.admin.base.entity.system.Role;
import com.admin.base.service.system.IRolePermissionService;
import com.admin.base.service.system.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private IRolePermissionService iRolePermissionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addRole(String note, String roleName, List<Integer> permissionIds) {
        if (checkExitsByRoleName(roleName)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该角色名称已存在");
        }
        Role role = EntityInit.initRole(roleName, note);
        //插入角色
        this.baseMapper.insertRole(role);
        //插入角色权限
        iRolePermissionService.insertRolePermission(role.getRoleId(), permissionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer roleId) {
        //删除角色下存在的权限
        iRolePermissionService.deleteByRoleId(roleId);
        //删除角色
        this.baseMapper.deleteById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Integer roleId, String roleName, String note, List<Integer> permissionIds) {
        final Role role = this.baseMapper.selectById(roleId);
        if (checkExitsByRoleName(roleName) && !roleId.equals(role.getRoleId())) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该角色名称已存在");
        }
        if (!roleName.equals(role.getRoleName())) {
            //更新角色名
            UpdateWrapper<Role> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(Role::getRoleId, role.getRoleId())
                    .set(Role::getRoleName, roleName)
                    .set(Role::getUpdateTime, LocalDateTime.now());
            this.baseMapper.update(null, updateWrapper);
        }
        if (!note.equals(role.getNote())) {
            //更新备注
            UpdateWrapper<Role> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(Role::getRoleId, role.getRoleId())
                    .set(Role::getNote, note)
                    .set(Role::getUpdateTime, LocalDateTime.now());
            this.baseMapper.update(null, updateWrapper);
        }
        //更新权限列表
        iRolePermissionService.updateRolePermission(roleId, permissionIds);
    }

    @Override
    public boolean checkExitsByRoleName(String roleName) {
        QueryWrapper<Role> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.lambda().eq(Role::getRoleName, roleName);
        final int count = Math.toIntExact(this.baseMapper.selectCount(roleQueryWrapper));
        return count > 0;
    }

    @Override
    public List<Role> getRoleList() {
        //获取角色列表
        return this.baseMapper.selectList(null);
    }


    @Override
    public PageResult<Role> getRolePage(Integer page, Integer size) {
        IPage<Role> iPage = new Page<>(page, size);
        IPage<Role> pageResult = this.baseMapper.selectPage(iPage, null);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }


}
