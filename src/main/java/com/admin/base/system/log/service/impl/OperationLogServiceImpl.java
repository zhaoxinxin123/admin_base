package com.admin.base.system.log.service.impl;

import com.admin.base.shared.api.PageResult;
import com.admin.base.system.log.dto.OperationLogListParam;
import com.admin.base.system.log.entity.OperationLog;
import com.admin.base.system.log.repository.OperationLogRepository;
import com.admin.base.system.log.service.IOperationLogService;
import com.admin.base.shared.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements IOperationLogService {

    private static final String OPERATION_TIME_PROPERTY = "operationTime";

    private final OperationLogRepository operationLogRepository;

    @Override
    public void insertOperationLog(OperationLog operationLog) {
        operationLogRepository.save(operationLog);
    }

    @Override
    public PageResult<OperationLog> listPage(OperationLogListParam operationListParam) {
        Pageable pageable = PageRequest.of(
                operationListParam.getPage() - 1,
                operationListParam.getSize(),
                // Spring Data Sort 接收实体属性名字符串；JPA 静态元模型未启用，所以用常量集中管理。
                Sort.by(Sort.Direction.DESC, OPERATION_TIME_PROPERTY));
        Page<OperationLog> page = operationLogRepository.findAll(specification(operationListParam), pageable);
        return new PageResult<>(page.getContent(), page.getTotalElements(), operationListParam.getPage(), operationListParam.getSize());
    }

    @Override
    public void deleteByIds(List<Integer> logIds) {
        if (logIds != null && !logIds.isEmpty()) {
            operationLogRepository.deleteAllById(logIds.stream().map(Integer::longValue).toList());
        }
    }

    private Specification<OperationLog> specification(OperationLogListParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!StringUtils.isEmpty(param.getOperationName())) {
                predicates.add(cb.equal(root.get("operationName"), param.getOperationName()));
            }
            if (param.getOperationType() != null) {
                predicates.add(cb.equal(root.get("businessType"), param.getOperationType()));
            }
            if (!StringUtils.isEmpty(param.getModelName())) {
                predicates.add(cb.equal(root.get("title"), param.getModelName()));
            }
            if (param.getStartTime() != null) {
                predicates.add(cb.greaterThan(root.get("operationTime"), param.getStartTime()));
            }
            if (param.getEndTime() != null) {
                predicates.add(cb.lessThan(root.get("operationTime"), param.getEndTime()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
