package com.admin.base.exception;

import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class BusinessExceptionTest {

    /**
     * 测试 BusinessException 构造时传入的 ResponseCode 和自定义消息能否被正确保留在
     * 异常对象的 getCode() 和 getMessage() 上。
     */
    @Test
    void carriesCodeAndMessage() {
        BusinessException ex = new BusinessException(ResponseCode.CODE_SYS_ERROR, "系统繁忙");
        assertThat(ex.getCode()).isEqualTo(ResponseCode.CODE_SYS_ERROR);
        assertThat(ex.getMessage()).isEqualTo("系统繁忙");
    }

    /**
     * 测试 BusinessException 属于 RuntimeException 子类，可被当作未检查异常抛出，
     * 并验证抛出时消息文本保持不变。
     */
    @Test
    void isRuntimeException() {
        assertThatThrownBy(() -> {
            throw new BusinessException(ResponseCode.CODE_ALERT, "test error");
        }).isInstanceOf(RuntimeException.class)
                .hasMessage("test error");
    }
}