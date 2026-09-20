package com.hess.metrichive.dto;

import com.hess.metrichive.Model.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TenantResponse {
    private Long id;
    private String name;
    private String email;
    private String apiKey;
    private String tier;
    private Integer maxMetricsPerDay;
    private Integer maxRequestsPerMinute;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantResponse from(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .email(tenant.getEmail())
                .apiKey(tenant.getApiKey())
                .tier(tenant.getTier())
                .maxMetricsPerDay(tenant.getMaxMetricsPerDay())
                .maxRequestsPerMinute(tenant.getMaxRequestsPerMinute())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
