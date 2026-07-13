package com.admin.base.shared.api;

import com.admin.base.shared.constant.ResponseCode;
import lombok.Data;

@Data
public class JsonResponse<T> {

    private Integer code;
    private String msg;
    private T data;

    public static JsonResponse<Void> success() {
        JsonResponse<Void> response = new JsonResponse<>();
        response.setCode(ResponseCode.CODE_OK);
        response.setMsg("成功");
        return response;
    }

    public static <T> JsonResponse<T> success(T data) {
        JsonResponse<T> response = new JsonResponse<>();
        response.setCode(ResponseCode.CODE_OK);
        response.setMsg("成功");
        response.setData(data);
        return response;
    }

    public static <T> JsonResponse<T> success(T data, String msg) {
        JsonResponse<T> response = new JsonResponse<>();
        response.setCode(ResponseCode.CODE_OK);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    public static JsonResponse<Void> error(Integer code, String msg) {
        JsonResponse<Void> response = new JsonResponse<>();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }

}