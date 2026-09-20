package com.hess.metrichive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class QueryMetadata {
    private Integer totalResults;
    private Boolean cached;
    private Double cacheHitRate;
    private Long queryTimeMs;
    private Long rowsScanned;
    private String requestId;
}