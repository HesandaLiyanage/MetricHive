package com.hess.metrichive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
public class QueryRequest {

    @NotBlank(message = "Metric Name is required")
    public String metric_name;

    @NotBlank(message = "Aggregation is required")
    public String aggregation;

    @NotNull(message = "Start time is required")
    public LocalDateTime start_time;
    
    @NotNull(message = "End time is required")
    private Instant endTime;

    public Optional<String> interval;
    public String[] groupBy;

    public Optional<List<String>> Filters;

}
