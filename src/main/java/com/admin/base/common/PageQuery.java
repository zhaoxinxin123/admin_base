package com.admin.base.common;

public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
    }

    public long offset() {
        return (long) (page - 1) * size;
    }
}