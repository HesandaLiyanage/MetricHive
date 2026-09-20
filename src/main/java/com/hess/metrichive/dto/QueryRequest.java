package com.hess.metrichive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@tools.jackson.databind.annotation.JsonNaming(tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
@com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class QueryRequest {

    @NotBlank(message = "Metric Name is required")
    private String metricName;

    @Pattern(regexp = "^(?i)(avg|sum|min|max|count)$", message = "Aggregation must be avg, sum, min, max, or count")
    @NotBlank(message = "Aggregation is required")
    private String aggregation;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    @NotNull(message = "Interval is required")
    @Pattern(regexp = "^(1m|5m|1h|1d|1w)$", message = "Interval must be 1m, 5m, 1h, 1d, or 1w")
    private String interval;

    private List<String> groupBy;

    private Map<String, String> filters;

    @Pattern(regexp = "^(value|timestamp|count)$", message = "OrderBy must be value, timestamp, or count")
    private String orderBy; // value, timestamp, count

    @Pattern(regexp = "^(asc|desc)$", message = "Order must be asc or desc")
    private String order; // asc, desc

    private Integer limit = 100; // default 100, max 1000



}
