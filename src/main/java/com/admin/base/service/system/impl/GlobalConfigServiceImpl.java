package com.admin.base.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.admin.base.component.EntityInit;
import com.admin.base.dto.request.system.AddGlobalConfigParam;
import com.admin.base.dto.request.system.UpdateGlobalConfigParam;
import com.admin.base.constant.ResponseCode;
import com.admin.base.entity.system.GlobalConfig;
import com.admin.base.exception.BusinessException;
import com.admin.base.mapper.SysGlobalConfigMapper;
import com.admin.base.service.system.IGlobalConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.base.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * tb_global_config 服务实现类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-23
 */
@Service
public class GlobalConfigServiceImpl extends ServiceImpl<SysGlobalConfigMapper, GlobalConfig> implements IGlobalConfigService {

    @Override
    public IPage<GlobalConfig> selectByPage(Integer page, Integer size, String key, String note) {
        IPage<GlobalConfig> iPage = new Page<>(page, size);
        QueryWrapper<GlobalConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(!StringUtils.isEmpty(key), GlobalConfig::getConfigKey, key)
                .like(!StringUtils.isEmpty(note), GlobalConfig::getNote, note)
                .orderByDesc(GlobalConfig::getCreateTime);
        return this.baseMapper.selectPage(iPage, queryWrapper);
    }

    @Override
    public void add(AddGlobalConfigParam addGlobalConfigParam) {
        final GlobalConfig globalConfig = selectByKey(addGlobalConfigParam.getKey());
        if (globalConfig != null) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该key值已存在");
        }
        GlobalConfig newGlobalConfig = EntityInit.initSysGlobalConfig(addGlobalConfigParam);
        this.baseMapper.insert(newGlobalConfig);
    }

    @Override
    public GlobalConfig selectByKey(String key) {
        QueryWrapper<GlobalConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GlobalConfig::getConfigKey, key);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Integer> configIds) {
        if (configIds.size() > 0) {
            this.baseMapper.deleteBatchIds(configIds);
        }
    }

    @Override
    public void updateConfig(UpdateGlobalConfigParam updateGlobalConfigParam) {
        final GlobalConfig globalConfigById = this.baseMapper.selectById(updateGlobalConfigParam.getConfigId());
        final GlobalConfig globalConfigByKey = selectByKey(updateGlobalConfigParam.getKey());
        if (globalConfigByKey != null && !globalConfigById.getConfigId().equals(globalConfigByKey.getConfigId())) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该key值已存在");
        }
        UpdateWrapper<GlobalConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(GlobalConfig::getConfigId, updateGlobalConfigParam.getConfigId())
                .set(GlobalConfig::getConfigKey, updateGlobalConfigParam.getKey())
                .set(GlobalConfig::getConfigValue, updateGlobalConfigParam.getValue())
                .set(GlobalConfig::getNote, updateGlobalConfigParam.getNote())
                .set(GlobalConfig::getUpdateTime,LocalDateTime.now());
        this.baseMapper.update(null, updateWrapper);
    }
}
