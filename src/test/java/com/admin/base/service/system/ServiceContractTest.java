package com.admin.base.service.system;

import com.admin.base.system.admin.application.IAdminRoleService;
import com.admin.base.system.admin.application.IAdminService;
import com.admin.base.system.config.application.IGlobalConfigService;
import com.admin.base.system.log.application.IOperationLogService;
import com.admin.base.system.permission.application.IPermissionsService;
import com.admin.base.system.role.application.IRolePermissionService;
import com.admin.base.system.role.application.IRoleService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceContractTest {

    private final List<Class<?>> serviceTypes = List.of(
            IAdminService.class,
            IAdminRoleService.class,
            IGlobalConfigService.class,
            IOperationLogService.class,
            IPermissionsService.class,
            IRolePermissionService.class,
            IRoleService.class
    );

    @Test
    void systemServiceInterfacesDoNotExtendMybatisPlusIService() {
        for (Class<?> serviceType : serviceTypes) {
            assertThat(List.of(serviceType.getInterfaces()))
                    .as(serviceType.getName())
                    .noneMatch(type -> type.getName().equals("com.baomidou.mybatisplus.extension.service.IService"));
        }
    }

    @Test
    void systemServiceInterfacesDoNotExposeIPage() {
        for (Class<?> serviceType : serviceTypes) {
            for (Method method : serviceType.getMethods()) {
                assertThat(method.getReturnType())
                        .as(serviceType.getSimpleName() + "." + method.getName())
                        .matches(type -> !type.getName().equals("com.baomidou.mybatisplus.core.metadata.IPage"));
            }
        }
    }
}
