package com.hess.metrichive.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class IngestRequest {

    @NotEmpty(message = "Metrics list cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 metrics per request")
    @Valid //this ensures rules inside metricdto is also applied
    private List<MetricDTO> metrics;
}