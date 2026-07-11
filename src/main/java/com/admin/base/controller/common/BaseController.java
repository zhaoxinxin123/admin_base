package com.admin.base.controller.common;

import com.admin.base.common.PageResult;
import com.admin.base.config.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/19 11:04 上午
 * @desc 通用信息
 */
@Controller
public class BaseController {

    @Autowired
    private CurrentUserProvider currentUserProvider;

    /**
     * 解析表格数据   列表查询内容 Service中统一返回PageResult
     *
     * @param pageResult            表格内容
     * @param dataMapInitialCapacity 表格容量
     * @return 获取数据表格
     */
    protected Map<String, Object> getDataTable(PageResult<?> pageResult, int dataMapInitialCapacity) {
        Map<String, Object> data = new HashMap<>(dataMapInitialCapacity);
        data.put("rows", pageResult.rows());
        data.put("total", pageResult.total());
        return data;
    }

    /**
     * 获取用户名
     *
     * @return 返回用户名
     */
    protected String getUserName() {
        return currentUserProvider.currentUser().username();
    }

}
