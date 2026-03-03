package com.hess.metrichive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "^(value|timestamp|count)$", message = "OrderBy must be value, timestamp, or count")
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

    private Integer limit; // default 100, max 1000

    public String cacheKey() {
        return String.format("%s:%s:%s:%s:%s",
                metricName, aggregation, startTime, endTime, interval);
    }

}
