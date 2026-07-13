package com.admin.base.system.admin.dto;

import com.admin.base.shared.api.dto.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 4:14 下午
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListAdminParam extends PageParam {
    /**
     * 根据账号查找
     */
    private String userName;

}
