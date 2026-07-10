package com.admin.base.service.system.impl;

import com.admin.base.common.PageResult;
import com.admin.base.component.EntityInit;
import com.admin.base.constant.ResponseCode;
import com.admin.base.dto.request.system.AddGlobalConfigParam;
import com.admin.base.dto.request.system.UpdateGlobalConfigParam;
import com.admin.base.entity.system.GlobalConfig;
import com.admin.base.exception.BusinessException;
import com.admin.base.repository.system.GlobalConfigRepository;
import com.admin.base.service.system.IGlobalConfigService;
import com.admin.base.utils.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalConfigServiceImpl implements IGlobalConfigService {

    private final GlobalConfigRepository globalConfigRepository;

    @Override
    public PageResult<GlobalConfig> selectByPage(Integer page, Integer size, String key, String note) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<GlobalConfig> pageResult = globalConfigRepository.findAll(specification(key, note), pageable);
        return new PageResult<>(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    @Override
    public void add(AddGlobalConfigParam addGlobalConfigParam) {
        if (globalConfigRepository.existsByConfigKey(addGlobalConfigParam.getKey())) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该key值已存在");
        }
        globalConfigRepository.save(EntityInit.initSysGlobalConfig(addGlobalConfigParam));
    }

    @Override
    public GlobalConfig selectByKey(String key) {
        return globalConfigRepository.findByConfigKey(key).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Integer> configIds) {
        if (configIds != null && !configIds.isEmpty()) {
            globalConfigRepository.deleteAllById(configIds.stream().map(Integer::longValue).toList());
        }
    }

    @Override
    public void updateConfig(UpdateGlobalConfigParam updateGlobalConfigParam) {
        Long configId = toLongId(updateGlobalConfigParam.getConfigId());
        GlobalConfig globalConfigById = globalConfigRepository.findById(configId)
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_ALERT, "配置项不存在"));
        GlobalConfig globalConfigByKey = selectByKey(updateGlobalConfigParam.getKey());
        if (globalConfigByKey != null && !globalConfigById.getConfigId().equals(globalConfigByKey.getConfigId())) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该key值已存在");
        }
        globalConfigById.setConfigKey(updateGlobalConfigParam.getKey());
        globalConfigById.setConfigValue(updateGlobalConfigParam.getValue());
        globalConfigById.setNote(updateGlobalConfigParam.getNote());
        globalConfigRepository.save(globalConfigById);
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }

    private Specification<GlobalConfig> specification(String key, String note) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!StringUtils.isEmpty(key)) {
                predicates.add(cb.like(root.get("configKey"), "%" + key + "%"));
            }
            if (!StringUtils.isEmpty(note)) {
                predicates.add(cb.like(root.get("note"), "%" + note + "%"));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
