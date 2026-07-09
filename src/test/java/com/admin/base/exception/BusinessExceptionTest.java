package com.admin.base.exception;

import com.admin.base.constant.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessExceptionTest {

    @Test
    void carriesCodeAndMessage() {
        BusinessException ex = new BusinessException(ResponseCode.CODE_SYS_ERROR, "系统繁忙");

        assertThat(ex.getCode()).isEqualTo(ResponseCode.CODE_SYS_ERROR);
        assertThat(ex.getMessage()).isEqualTo("系统繁忙");
    }

    @Test
    void isRuntimeException() {
        assertThatThrownBy(() -> {
            throw new BusinessException(ResponseCode.CODE_ALERT, "test error");
        }).isInstanceOf(RuntimeException.class)
                .hasMessage("test error");
    }
}