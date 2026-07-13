package com.admin.base.shared.exception;

import com.admin.base.shared.api.JsonResponse;
import com.admin.base.shared.constant.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public JsonResponse<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied", e);
        return JsonResponse.error(ResponseCode.CODE_TOKEN_ERROR, "没有权限访问");
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public JsonResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return JsonResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public JsonResponse<Void> handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("参数错误");
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, message);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public JsonResponse<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed", e);
        return JsonResponse.error(ResponseCode.CODE_NO_LOGIN, "请先登录");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResponse<Void> handleGlobal(Exception e) {
        log.error("Unhandled exception", e);
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "系统繁忙");
    }

}