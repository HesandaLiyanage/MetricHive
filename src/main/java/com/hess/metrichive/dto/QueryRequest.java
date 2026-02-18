package com.hess.metrichive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
public class QueryRequest {

    @NotBlank(message = "API key is required")
    private String apiKey;

    public String metric_name;
    public String aggregation;

    public LocalDateTime start_time;
    public LocalDateTime end_time;

    public Optional<String> interval;
    public String[] groupBy;

    public Optional<List<String>> Filters;

}
