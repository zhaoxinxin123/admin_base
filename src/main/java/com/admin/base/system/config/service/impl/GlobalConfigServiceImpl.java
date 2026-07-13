package com.admin.base.system.config.service.impl;

import com.admin.base.shared.api.PageResult;
import com.admin.base.shared.factory.EntityFactory;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.system.config.dto.AddGlobalConfigParam;
import com.admin.base.system.config.dto.UpdateGlobalConfigParam;
import com.admin.base.system.config.entity.GlobalConfig;
import com.admin.base.shared.exception.BusinessException;
import com.admin.base.system.config.repository.GlobalConfigRepository;
import com.admin.base.system.config.service.IGlobalConfigService;
import com.admin.base.shared.util.StringUtils;
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

    private static final String CREATE_TIME_PROPERTY = "createTime";

    private final GlobalConfigRepository globalConfigRepository;

    @Override
    public PageResult<GlobalConfig> selectByPage(Integer page, Integer size, String key, String note) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, CREATE_TIME_PROPERTY));
        Page<GlobalConfig> pageResult = globalConfigRepository.findAll(specification(key, note), pageable);
        return new PageResult<>(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    @Override
    public void add(AddGlobalConfigParam addGlobalConfigParam) {
        if (globalConfigRepository.existsByConfigKey(addGlobalConfigParam.getKey())) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "该key值已存在");
        }
        globalConfigRepository.save(EntityFactory.initSysGlobalConfig(addGlobalConfigParam));
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

    /**
     * 构建配置列表动态查询条件。
     *
     * <p>这里的 Specification 等价于原 MyBatis QueryWrapper 的动态 where：
     * key 和 note 为空时不追加条件；有值时使用 LIKE 做模糊查询。</p>
     */
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
