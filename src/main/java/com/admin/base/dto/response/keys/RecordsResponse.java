package com.admin.base.dto.response.keys;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/3 11:55
 * @desc
 */
@Data
public class RecordsResponse {
    /**
     * 关键词ID
     */

    private Integer id;
    /**
     * 上传文件名
     */
    private String fileName;
    /**
     * 关键词列表
     */
    private String keyWordList;
    /**
     * 识别结果
     */
    private String resultPath;

    /**
     * 是否有识别结果
     * 0:未匹配到关键词内容
     * 1：匹配到关键词内容
     */
    private Integer resultState;

    /**
     * 创建时间
     */
    private LocalDateTime creteTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
