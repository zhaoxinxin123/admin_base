package com.admin.base.common;

import com.admin.base.constant.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResponseTest {

    @Test
    void successKeepsCompatibleShape() {
        JsonResponse response = JsonResponse.success("ok");

        assertThat(response.getCode()).isEqualTo(ResponseCode.CODE_OK);
        assertThat(response.getMsg()).isEqualTo("成功");
        assertThat(response.getData()).isEqualTo("ok");
    }

    @Test
    void errorKeepsCompatibleShape() {
        JsonResponse response = JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "系统繁忙");

        assertThat(response.getCode()).isEqualTo(ResponseCode.CODE_SYS_ERROR);
        assertThat(response.getMsg()).isEqualTo("系统繁忙");
        assertThat(response.getData()).isNull();
    }
}