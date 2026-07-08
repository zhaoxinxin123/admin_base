package com.admin.base.dto.request.keys;

import com.admin.base.dto.request.common.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/4/3 11:49
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SelectRecordParam extends PageParam {

    private String fileName;
}
