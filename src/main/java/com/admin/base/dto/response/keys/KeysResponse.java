package com.admin.base.dto.response.keys;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/1 10:17
 * @desc
 */
@Data
public class KeysResponse {
    private Integer id;

    /**
     * 关键词
     */
    private String keyWord;
    /**
     * 备注
     */
    private String note;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
