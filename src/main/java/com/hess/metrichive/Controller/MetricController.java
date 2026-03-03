package com.hess.metrichive.Controller;

import com.hess.metrichive.dto.IngestRequest;
import com.hess.metrichive.dto.IngestResponse;
import com.hess.metrichive.Security.TenantContext;
import com.hess.metrichive.Service.MetricIngestionService;
import com.hess.metrichive.dto.QueryRequest;
import com.hess.metrichive.dto.QueryResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metrics")
@Slf4j
public class MetricController {

    @Autowired
    private MetricIngestionService metricIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingest(@RequestBody @Valid IngestRequest request) {

        // Start timer
        long startTime = System.currentTimeMillis();

        // Get current tenant (from API key authentication)
        Long tenantId = TenantContext.getTenantId();

        // Log
        log.info("Ingesting {} metrics for tenant {}",
                request.getMetrics().size(), tenantId);

        // Call service to do the actual work
        metricIngestionService.ingestBatch(request.getMetrics());

        // Calculate processing time
        long processingTime = System.currentTimeMillis() - startTime;

        // Return response
        return ResponseEntity.ok(IngestResponse.builder()
                .status("success")
                .metricsReceived(String.valueOf(request.getMetrics().size()))
                .processingTimeMs(processingTime)
                .build());
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> Query (@PathVariable QueryRequest request) {
        return ResponseEntity.ok(QueryResponse.builder()
                .status("success"))

    }
}