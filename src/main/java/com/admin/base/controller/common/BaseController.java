package com.admin.base.controller.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.base.config.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
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
    /**
     * 解析表格数据   列表查询内容 Service中统一返回Ipage
     *
     * @param pageInfo               表格内容
     * @param dataMapInitialCapacity 表格容量
     * @return 获取数据表格
     */
    protected Map<String, Object> getDataTable(IPage<?> pageInfo, int dataMapInitialCapacity) {
        Map<String, Object> data = new HashMap<>(dataMapInitialCapacity);
        data.put("rows", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        return data;
    }

    /**
     * 获取用户名
     *
     * @return 返回用户名
     */
    protected String getUserName() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getDetails()).getUsername();
    }

}
