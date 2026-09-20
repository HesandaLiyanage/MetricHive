package com.hess.metrichive.Exception;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@tools.jackson.databind.annotation.JsonNaming(tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
@com.fasterxml.jackson.databind.annotation.JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErrorResponse {
    @Builder.Default
    private String status = "error";
    private String errorCode;
    private String message;
    private Instant timestamp;
    private String requestId; // We will populate this automatically in Phase 4 (Observability)
}
