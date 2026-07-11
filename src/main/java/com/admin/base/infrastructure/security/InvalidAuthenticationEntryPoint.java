package com.admin.base.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * 认证入口点：当未认证用户访问受保护资源时触发，
 * 返回 HTTP 401 状态码与 JSON 错误体。
 */
@Component
public class InvalidAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 先设置 HTTP 401 状态码，再通过 HandlerExceptionResolver 写入 JSON 错误体，
        // 确保前后端分离架构下匿名请求返回标准 REST 401 语义
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resolver.resolveException(request, response, null, authException);
    }

}
