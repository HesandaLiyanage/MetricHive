package com.hess.metrichive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    private String metricName;
    private String aggregation;
    private Instant startTime;
    private Instant endTime;
    private String interval;
    private List<QueryResult> results;
    private QueryMetadata metadata;

}
