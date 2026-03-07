package com.hess.metrichive.Controller;

import com.hess.metrichive.dto.IngestRequest;
import com.hess.metrichive.dto.IngestResponse;
import com.hess.metrichive.Security.TenantContext;
import com.hess.metrichive.Service.MetricIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricController {

    private final MetricIngestionService metricIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody IngestRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Get current tenant from the security context
        Long tenantId = TenantContext.getTenantId();

        log.info("Received {} metrics for ingestion from tenant {}", request.getMetrics().size(), tenantId);

        // 2. Pass to service layer
        metricIngestionService.ingestBatch(tenantId, request.getMetrics());

        // 3. Formulate Response
        long processingTime = System.currentTimeMillis() - startTime;

        IngestResponse response = IngestResponse.builder()
                .status("success")
                .metricsReceived(request.getMetrics().size())
                .processingTimeMs(processingTime)
                .build();

        return ResponseEntity.ok(response);
    }
}