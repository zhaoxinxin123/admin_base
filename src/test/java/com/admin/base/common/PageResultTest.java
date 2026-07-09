package com.admin.base.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void exposesRowsAndTotalForLegacyTableShape() {
        PageResult<String> result = new PageResult<>(List.of("a", "b"), 12, 2, 2);

        assertThat(result.rows()).containsExactly("a", "b");
        assertThat(result.total()).isEqualTo(12);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
    }
}