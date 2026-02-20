package com.hess.metrichive.Controller;


import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Service.MetricIngestionService;
import com.hess.metrichive.Service.TenantService;
import com.hess.metrichive.dto.IngestRequest;
import com.hess.metrichive.dto.QueryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricController {
    public final MetricIngestionService metricIngestionService;
    public final TenantService tenantService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestMetrics(@Valid @RequestBody IngestRequest request) {
        log.info("Got a request to ingestion with {} metrics" , request.getMetrics());


        Tenant tenant = tenantService.findApiKey(request.getApiKey()).orElseThrow(
                () -> new RuntimeException("Invalid API Key!")
        );

        metricIngestionService.ingestMetrics(tenant.getId(), request.getMetrics());

        return ResponseEntity.ok().body(Map.of(
                "status" , "success",
                "ingested" , request.getMetrics().size()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<?>  health() {
        return ResponseEntity.ok(Map.of("status" , "healthy"));
    }

    @PostMapping("/query")
    public ResponseEntity<?> queryMetrics(@Valid @RequestBody QueryRequest request) {

    }



}
