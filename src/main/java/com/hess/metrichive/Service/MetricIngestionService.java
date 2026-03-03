package com.hess.metrichive.Service;

import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.dto.MetricDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetricIngestionService {

    private final MetricRepository metricRepository;
    public MetricIngestionService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }


    public void ingestMetrics(Long tenantId, List<MetricDTO> metricDTOs) {
        log.info("Ingesting {} metrics for tenant {}" , metricDTOs.size());

        List<Metric> metrics = metricDTOs.stream()
                .map(dto -> convertToEntity(tenantId,dto))
                .collect(Collectors.toList());
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

    public void ingestBatch(@NotEmpty(message = "Metrics list cannot be empty") @Size(max = 1000, message = "Maximum 1000 metrics per request") @Valid List<MetricDTO> metrics) {
    }
}