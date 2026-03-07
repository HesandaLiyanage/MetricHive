package com.hess.metrichive.Exception;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.class)

public class ErrorResponse {
    private String status = "error";
    private String errorCode;
    private String message;
    private Instant timestamp;
    private String requestId; // We will populate this automatically in Phase 4 (Observability)
}
