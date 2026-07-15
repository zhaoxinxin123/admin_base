package com.admin.base.system.permission;

import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.permission.dto.UpdatePermissionParam;
import com.admin.base.system.permission.entity.Permissions;
import com.admin.base.system.permission.repository.PermissionsRepository;
import com.admin.base.system.permission.service.impl.PermissionsServiceImpl;
import com.admin.base.system.role.service.IRolePermissionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionsServiceImplTest {

    private final PermissionsRepository repository = mock(PermissionsRepository.class);
    private final IRolePermissionService rolePermissionService = mock(IRolePermissionService.class);
    private final PermissionsServiceImpl service = new PermissionsServiceImpl(repository, rolePermissionService);

    @Test
    void updateRejectsSelfAsParent() {
        Permissions permission = permission(1, 0, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(permission));
        UpdatePermissionParam param = updateParam(1, 1);

        assertThatThrownBy(() -> service.updatePermission(param))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("父级");
    }

    @Test
    void updateRejectsDescendantAsParent() {
        Permissions root = permission(1, 0, 1);
        Permissions child = permission(2, 1, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(root));
        when(repository.findById(2L)).thenReturn(Optional.of(child));

        assertThatThrownBy(() -> service.updatePermission(updateParam(1, 2)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("父级");
    }

    @Test
    void deleteRemovesRoleMappingsForEntireSubtree() {
        Permissions root = permission(1, 0, 1);
        Permissions child = permission(2, 1, 2);
        Permissions grandchild = permission(3, 2, 3);
        when(repository.findById(1L)).thenReturn(Optional.of(root));
        when(repository.findByParentId(1L)).thenReturn(List.of(child));
        when(repository.findByParentId(2L)).thenReturn(List.of(grandchild));
        when(repository.findByParentId(3L)).thenReturn(List.of());

        service.deleteById(1);

        verify(rolePermissionService).deleteByPermissionId(1);
        verify(rolePermissionService).deleteByPermissionId(2);
        verify(rolePermissionService).deleteByPermissionId(3);
        verify(repository).deleteAllById(List.of(1L, 2L, 3L));
    }

    private Permissions permission(long id, long parentId, int level) {
        Permissions permission = new Permissions();
        permission.setPermissionId(id);
        permission.setParentId(parentId);
        permission.setLevel(level);
        return permission;
    }

    private UpdatePermissionParam updateParam(int id, int parentId) {
        UpdatePermissionParam param = new UpdatePermissionParam();
        param.setPermissionId(id);
        param.setParentId(parentId);
        param.setPerm("sys:test:" + id);
        param.setTitle("Test");
        param.setPath("/test");
        param.setState(1);
        return param;
    }
}
