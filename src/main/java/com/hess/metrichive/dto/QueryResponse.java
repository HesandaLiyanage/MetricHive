package com.hess.metrichive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class QueryResponse {
    private String metricName;
    private String aggregation;
    private Instant startTime;
    private Instant endTime;
    private String interval;
    private List<QueryResult> results;
    private QueryMetadata metadata;

}
