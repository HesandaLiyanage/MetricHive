package com.hess.metrichive.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestResponse {
    public String status;
    public String metricsReceived;
    public long processingTimeMs;
    public String requestId;

}
