package com.hess.metrichive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String apiKey;

    @Pattern(regexp = "^(?i)(free|basic|pro|premium)$", message = "Tier must be free, basic, pro, or premium")
    private String tier;

    @Positive(message = "max_metrics_per_day must be positive")
    private Integer maxMetricsPerDay;

    @Positive(message = "max_requests_per_minute must be positive")
    private Integer maxRequestsPerMinute;
}
