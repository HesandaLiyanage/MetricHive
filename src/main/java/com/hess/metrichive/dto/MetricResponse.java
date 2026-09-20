package com.hess.metrichive.dto;

import com.hess.metrichive.Model.Metric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MetricResponse {
    private Long id;
    private Long tenantId;
    private String metricName;
    private Double value;
    private Instant timestamp;
    private Map<String, String> tags;
    private LocalDateTime createdAt;

    public static MetricResponse from(Metric metric) {
        if (metric == null) return null;
        return MetricResponse.builder()
                .id(metric.getId())
                .tenantId(metric.getTenantId())
                .metricName(metric.getMetricName())
                .value(metric.getValue())
                .timestamp(metric.getTimestamp())
                .tags(metric.getTags())
                .createdAt(metric.getCreatedAt())
                .build();
    }
}
