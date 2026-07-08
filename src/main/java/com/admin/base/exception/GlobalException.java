package com.admin.base.exception;

import com.admin.base.constant.ResponseCode;
import com.admin.base.common.JsonResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/24 11:29 下午
 * @desc
 */
@ControllerAdvice
public class GlobalException {


    /**
     * 监听没有权限异常
     *
     * @param e   异常
     * @return  没有权限访问异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public JsonResponse bindException(AccessDeniedException e) {
        e.printStackTrace();
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, e.getMessage());

    }

    /**
     * 业务异常
     *
     * @param businessException  业务异常
     * @return  报错
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public JsonResponse handleBusinessException(BusinessException businessException) {
        businessException.printStackTrace();
        return JsonResponse.error(businessException.getJsonResponse().getCode(), businessException.getJsonResponse().getMsg());
    }

    /**
     * 参数校验异常
     *
     * @param bindException  参数校验异常
     * @return    返回参数异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public JsonResponse bindException(BindException bindException) {
        bindException.printStackTrace();
        final List<ObjectError> allErrors = bindException.getAllErrors();
        for (ObjectError allError : allErrors) {
            return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, allError.getDefaultMessage());
        }
        return null;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResponse handleGlobal(Exception e) {
        e.printStackTrace();
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "系统繁忙");
    }

    @ExceptionHandler({ AuthenticationException.class })
    @ResponseBody
    public JsonResponse  handleAuthenticationException(Exception e) {
        e.printStackTrace();
        return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "Authentication failed at controller advice");
    }


}
