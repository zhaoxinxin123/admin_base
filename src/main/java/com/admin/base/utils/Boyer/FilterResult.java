package com.admin.base.utils.Boyer;

import lombok.Data;

import java.util.List;

@Data
public class FilterResult {
    /**
     * 段落数
     */
    private Integer paragraphNumber;
    /**
     * 段落中包含的敏感词汇
     */
    private List<String> sensitives;
    /**
     * 段落内容
     */
    private String paragraphContent;
    /**
     * 页码
     */
    private Integer pageNumber;

}
