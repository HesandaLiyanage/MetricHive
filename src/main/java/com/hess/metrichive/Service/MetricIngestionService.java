package com.hess.metrichive.Service;

import com.hess.metrichive.dto.MetricDTO;
import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Repository.MetricRepository;
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

    @Transactional
    public void ingestMetrics(Long tenantId, List<MetricDTO> metricDTOs) {
        log.info("Ingesting {} metrics for tenant {}", metricDTOs.size(), tenantId);

        List<Metric> metrics = metricDTOs.stream()
                .map(dto -> convertToEntity(tenantId, dto))
                .collect(Collectors.toList());

        // For now, simple save - we'll optimize this later with JDBC batch
        metricRepository.saveAll(metrics);

        log.info("Successfully ingested {} metrics", metrics.size());
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