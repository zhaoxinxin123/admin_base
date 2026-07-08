package com.admin.base.common;

import com.admin.base.constant.ResponseCode;
import lombok.Data;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/19 11:31 上午
 * @desc 统一响应
 */
@Data
public class JsonResponse {

    /**
     * 返回状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private Object data;


    /**
     * 返回成功数据
     *
     * @return 结果
     */
    public static JsonResponse success() {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setCode(ResponseCode.CODE_OK);
        jsonResponse.setMsg("成功");
        return jsonResponse;
    }


    /**
     * 返回成功数据
     *
     * @param data 数据
     * @return 结果
     */
    public static JsonResponse success(Object data) {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setCode(ResponseCode.CODE_OK);
        jsonResponse.setMsg("成功");
        jsonResponse.setData(data);
        return jsonResponse;
    }
    /**
     * 返回成功数据
     *
     * @param data 数据
     * @return 结果
     */
    public static JsonResponse success(Object data,String msg) {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setCode(ResponseCode.CODE_OK);
        jsonResponse.setMsg(msg);
        jsonResponse.setData(data);
        return jsonResponse;
    }

    /**
     * 返回失败数据
     *
     * @param code 状态码
     * @param msg  返回信息
     * @return 结果
     */
    public static JsonResponse error(Integer code, String msg) {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setCode(code);
        jsonResponse.setMsg(msg);
        return jsonResponse;
    }


}
