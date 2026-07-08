package com.admin.base.dto.request.system;

import com.admin.base.dto.request.common.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/23 8:14 下午
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListGlobalConfigParam extends PageParam{
    /**
     * 根据键值模糊搜索
     */
    private String key;
    /**
     * 参数备注
     */
    private String note;
}
