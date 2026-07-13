package com.admin.base.shared.api;

import com.admin.base.shared.constant.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResponseTest {

    /**
     * 测试 JsonResponse.success() 构造的成功响应：code=200、msg="成功"、data 为传入对象，
     * 确保对外 JSON 契约与历史前端约定一致。
     */
    @Test
    void successKeepsCompatibleShape() {
        JsonResponse<Object> response = JsonResponse.success("ok");

        assertThat(response.getCode()).isEqualTo(ResponseCode.CODE_OK);
        assertThat(response.getMsg()).isEqualTo("成功");
        assertThat(response.getData()).isEqualTo("ok");
    }

    /**
     * 测试 JsonResponse.error() 构造的错误响应：code 取传入的 ResponseCode、msg 为自定义消息、
     * data 为 null，验证错误响应的字段结构稳定。
     */
    @Test
    void errorKeepsCompatibleShape() {
        JsonResponse<Void> response = JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "系统繁忙");

        assertThat(response.getCode()).isEqualTo(ResponseCode.CODE_SYS_ERROR);
        assertThat(response.getMsg()).isEqualTo("系统繁忙");
        assertThat(response.getData()).isNull();
    }

    /**
     * 测试泛型 JsonResponse<String>.success() 能把业务数据原样放入 data 字段，
     * 验证强类型响应包装器对调用方透明。
     */
    @Test
    void genericResponseCarriesTypedData() {
        JsonResponse<String> response = JsonResponse.success("typed");

        assertThat(response.getData()).isEqualTo("typed");
    }
}