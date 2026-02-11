package com.hess.metrichive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
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
    private LocalDateTime timestamp;

    private Map<String, String> tags;
}