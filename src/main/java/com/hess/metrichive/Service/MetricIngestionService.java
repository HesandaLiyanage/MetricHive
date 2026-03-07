package com.hess.metrichive.Service;

import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.dto.MetricDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricIngestionService {

    private final MetricRepository metricRepository;

    @Transactional // Ensure the entire batch succeeds or fails together
    public void ingestBatch(Long tenantId, List<MetricDTO> metricDTOs) {

        // Transform DTOs to Entities and attach the Tenant ID
        List<Metric> entities = metricDTOs.stream()
                .map(dto -> convertToEntity(tenantId, dto))
                .collect(Collectors.toList());

        // Perform the batch insert
        metricRepository.batchInsert(entities);
    }

    private Metric convertToEntity(Long tenantId, MetricDTO dto) {
        Metric metric = new Metric();
        metric.setTenantId(tenantId);
        metric.setMetricName(dto.getName());
        metric.setValue(dto.getValue());
        metric.setTimestamp(dto.getTimestamp());
        metric.setTags(dto.getTags());
        return metric;
    }
}