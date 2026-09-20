package com.hess.metrichive.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateMetricRequest {

    @NotBlank(message = "Metric name is required")
    @Size(max = 255, message = "Metric name too long")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "invalid format name, should follow the format 'namespace.metric_name' ex:- sales.revenue")
    private String name;

    @NotNull(message = "Value is required")
    private Double value;

    @PastOrPresent(message = "Timestamp cannot be in the future")
    private Instant timestamp;

    private Map<
            @NotBlank(message = "Tag name is missing")
            String,

            @NotBlank(message = "Tag value cannot be empty")
            String
            > tags;
}
