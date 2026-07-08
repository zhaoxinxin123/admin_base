package com.admin.base.exception;

import com.admin.base.common.JsonResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/24 11:41 下午
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException{
    private JsonResponse jsonResponse;


    public BusinessException(JsonResponse jsonResponse) {
        this.jsonResponse = jsonResponse;
    }
}
