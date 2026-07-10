package com.admin.base.service.system;

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
