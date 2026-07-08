package com.admin.base.dto.request.keys;

import com.admin.base.dto.request.common.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/1 10:03
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SelectKeyParam extends PageParam {

    private String key;
}
