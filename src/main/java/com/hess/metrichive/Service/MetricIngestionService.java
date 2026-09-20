package com.hess.metrichive.Service;

import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Repository.MetricJpaRepository;
import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.dto.CreateMetricRequest;
import com.hess.metrichive.dto.MetricDTO;
import com.hess.metrichive.dto.MetricResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricIngestionService {

    private final MetricRepository metricRepository;
    private final MetricJpaRepository metricJpaRepository;

    @Transactional
    public void ingestBatch(Long tenantId, List<MetricDTO> metricDTOs) {
        if (metricDTOs == null || metricDTOs.isEmpty()) {
            return;
        }

        List<Metric> entities = metricDTOs.stream()
                .map(dto -> convertToEntity(tenantId, dto))
                .collect(Collectors.toList());

        metricRepository.batchInsert(entities);
        log.info("Successfully batch-inserted {} metrics for tenantId {}", entities.size(), tenantId);
    }

    @Transactional
    public MetricResponse createSingleMetric(Long tenantId, CreateMetricRequest request) {
        Instant timestamp = request.getTimestamp() != null ? request.getTimestamp() : Instant.now();

        Metric metric = Metric.builder()
                .tenantId(tenantId)
                .metricName(request.getName())
                .value(request.getValue())
                .timestamp(timestamp)
                .tags(request.getTags())
                .build();

        Metric saved = metricJpaRepository.save(metric);
        log.info("Created metric {} with id {} for tenantId {}", saved.getMetricName(), saved.getId(), tenantId);
        return MetricResponse.from(saved);
    }

    private Metric convertToEntity(Long tenantId, MetricDTO dto) {
        Instant timestamp = dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now();

        return Metric.builder()
                .tenantId(tenantId)
                .metricName(dto.getName())
                .value(dto.getValue())
                .timestamp(timestamp)
                .tags(dto.getTags())
                .build();
    }
}