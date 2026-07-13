package com.admin.base.shared.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    /**
     * 测试 PageResult 在兼容旧版前端表格结构时正确暴露 rows、total、page、size 四个字段，
     * 保证分页响应在迁移过程中与历史契约一致。
     */
    @Test
    void exposesRowsAndTotalForLegacyTableShape() {
        PageResult<String> result = new PageResult<>(List.of("a", "b"), 12, 2, 2);

        assertThat(result.rows()).containsExactly("a", "b");
        assertThat(result.total()).isEqualTo(12);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
    }
}