package com.admin.base.system.role;

import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.role.entity.Role;
import com.admin.base.system.role.repository.RoleRepository;
import com.admin.base.system.role.service.IRolePermissionService;
import com.admin.base.system.role.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleServiceImplTest {

    private final RoleRepository repository = mock(RoleRepository.class);
    private final IRolePermissionService rolePermissionService = mock(IRolePermissionService.class);
    private final RoleServiceImpl service = new RoleServiceImpl(repository, rolePermissionService);

    @Test
    void addRejectsRoleNameThatOnlyContainsPrefix() {
        assertThatThrownBy(() -> service.addRole("note", "XROLE_USER", List.of(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ROLE_开头");

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addRejectsRoleNameLongerThanTenCharacters() {
        assertThatThrownBy(() -> service.addRole("note", "ROLE_ABCDEF", List.of(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能大于10");
    }

    @Test
    void updateRejectsNameOwnedByAnotherRole() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("ROLE_ONE");
        when(repository.findById(1L)).thenReturn(Optional.of(role));
        when(repository.existsByRoleNameAndRoleIdNot("ROLE_TWO", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateRole(1, "ROLE_TWO", "note", List.of(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已存在");
    }
}
