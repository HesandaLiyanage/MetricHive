package com.hess.metrichive.Controller;


import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Service.MetricIngestionService;
import com.hess.metrichive.Service.TenantService;
import com.hess.metrichive.dto.IngestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        tenantService.findApiKey(request.getApiKey());
    }

}
