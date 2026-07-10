package com.admin.base.service.system.impl;

import com.admin.base.common.PageResult;
import com.admin.base.dto.request.system.OperationLogListParam;
import com.admin.base.entity.system.OperationLog;
import com.admin.base.repository.system.OperationLogRepository;
import com.admin.base.service.system.IOperationLogService;
import com.admin.base.utils.StringUtils;
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
                Sort.by(Sort.Direction.DESC, "operationTime"));
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
