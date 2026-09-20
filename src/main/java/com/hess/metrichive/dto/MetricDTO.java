package com.hess.metrichive.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MetricDTO {

    @NotBlank(message = "Metric name is required")
    @Size(max = 255, message = "Metric name too long")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$" , message = "invalid format name, should follow the format 'namespace.metric_name' ex:- sales.revenue")
    private String name;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    private Double value;

    @NotNull(message = "Timestamp is required")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    private Instant timestamp;

    private Map<
            @NotBlank(message = "Tag name is missing ")
            String,

            @NotBlank(message = "Tag value cannot be empty")
            String
            > tags;
}