package com.hess.metrichive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.aot.generate.QueryMetadata;

import java.time.Instant;
import java.util.List;

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
