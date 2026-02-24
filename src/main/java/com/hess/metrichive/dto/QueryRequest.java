package com.hess.metrichive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class QueryRequest {

    @NotBlank(message = "Metric Name is required")
    private String metricName;

    @NotBlank(message = "Aggregation is required")
    private String aggregation;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    private String interval;

    private List<String> groupBy;

    private Map<String, String> filters;

    private String orderBy; // value, timestamp, count

    private String order; // asc, desc

    private Integer limit; // default 100, max 1000

    public String cacheKey() {
        return String.format("%s:%s:%s:%s:%s",
                metricName, aggregation, startTime, endTime, interval);
    }

}
