package com.admin.base.controller.system;

import com.admin.base.common.JsonResponse;
import com.admin.base.dto.request.system.LoginParam;
import com.admin.base.dto.response.system.LoginResponse;
import com.admin.base.service.system.IAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@Slf4j
@RequestMapping("/open")
@RestController
public class LoginController {

    @Resource
    private IAdminService iAdminService;

    @PostMapping(value = "/login")
    public JsonResponse login( @RequestBody @Validated LoginParam loginParam) {
        LoginResponse loginResponse = iAdminService.login(loginParam);
        return JsonResponse.success(loginResponse);
    }

}
