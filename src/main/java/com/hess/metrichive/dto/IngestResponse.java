package com.hess.metrichive.dto;


import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class IngestResponse {
    public String status;
    public int metricsReceived;
    public long processingTimeMs;
    public String requestId;

}
