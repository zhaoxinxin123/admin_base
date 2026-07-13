package com.admin.base.shared.api;

import java.util.List;

public record PageResult<T>(List<T> rows, long total, int page, int size) {

    public PageResult {
        rows = rows == null ? List.of() : List.copyOf(rows);
        if (total < 0) {
            total = 0;
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = rows.size();
        }
    }
}