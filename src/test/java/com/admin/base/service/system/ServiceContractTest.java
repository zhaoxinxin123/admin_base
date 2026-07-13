package com.admin.base.service.system;

import com.admin.base.system.admin.service.IAdminRoleService;
import com.admin.base.system.admin.service.IAdminService;
import com.admin.base.system.config.service.IGlobalConfigService;
import com.admin.base.system.log.service.IOperationLogService;
import com.admin.base.system.permission.service.IPermissionsService;
import com.admin.base.system.role.service.IRolePermissionService;
import com.admin.base.system.role.service.IRoleService;
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

    /**
     * 测试系统模块（admin/role/permission/config/log 等）的 Service 接口都不应再继承
     * MyBatis Plus 的 com.baomidou.mybatisplus.extension.service.IService，确保业务边界
     * 不依赖旧持久层。
     */
    @Test
    void systemServiceInterfacesDoNotExtendMybatisPlusIService() {
        for (Class<?> serviceType : serviceTypes) {
            assertThat(List.of(serviceType.getInterfaces()))
                    .as(serviceType.getName())
                    .noneMatch(type -> type.getName().equals("com.baomidou.mybatisplus.extension.service.IService"));
        }
    }

    /**
     * 测试系统模块的 Service 接口方法返回值不再使用 MyBatis Plus 的 IPage 类型，
     * 防止分页对象泄漏到 controller/service 边界，所有分页统一由 JPA + PageResult 承担。
     */
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
