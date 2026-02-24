package com.hess.metrichive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMetadata {
    private Integer totalResults;
    private Boolean cached;
    private Double cacheHitRate;
    private Long queryTimeMs;
    private Long rowsScanned;
    private String requestId;
}