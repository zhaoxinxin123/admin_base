package com.admin.base.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.base.dto.request.system.AddGlobalConfigParam;
import com.admin.base.dto.request.system.UpdateGlobalConfigParam;
import com.admin.base.entity.system.GlobalConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * tb_global_config 服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
public interface IGlobalConfigService extends IService<GlobalConfig> {
    /**
     * 分页查询配置项
     *
     * @param page 当前页码
     * @param size 每页大小
     * @param key  键值
     * @param note 值
     * @return iPage
     */
    IPage<GlobalConfig> selectByPage(Integer page, Integer size, String key, String note);

    /**
     * 新增全局配置
     *
     * @param addGlobalConfigParam 参数
     */
    void add(AddGlobalConfigParam addGlobalConfigParam);

    /**
     * 根据键值查询全局配置
     *
     * @param key 键值
     * @return SysGlobalConfig
     */
    GlobalConfig selectByKey(String key);

    /**
     * 批量删除配置项
     *
     * @param configIds 配置项id列表
     */
    void deleteByIds(List<Integer> configIds);

    /**
     * 更新配置项
     * @param updateGlobalConfigParam  参数
     */
    void updateConfig(UpdateGlobalConfigParam updateGlobalConfigParam);
}
