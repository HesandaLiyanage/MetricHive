package com.hess.metrichive.Controller;

import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Security.TenantContext;
import com.hess.metrichive.Service.MetricIngestionService;
import com.hess.metrichive.Service.MetricQueryService;
import com.hess.metrichive.dto.CreateMetricRequest;
import com.hess.metrichive.dto.IngestRequest;
import com.hess.metrichive.dto.IngestResponse;
import com.hess.metrichive.dto.MetricResponse;
import com.hess.metrichive.dto.PageResponse;
import com.hess.metrichive.dto.QueryRequest;
import com.hess.metrichive.dto.QueryResponse;
import com.hess.metrichive.dto.UpdateMetricRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricController {

    private final MetricIngestionService metricIngestionService;
    private final MetricQueryService metricQueryService;

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingestBatch(@Valid @RequestBody IngestRequest request) {
        long startTime = System.currentTimeMillis();

        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        log.info("Received {} metrics for ingestion from tenant {}", request.getMetrics().size(), tenantId);

        metricIngestionService.ingestBatch(tenantId, request.getMetrics());

        long processingTime = System.currentTimeMillis() - startTime;
        String requestId = MDC.get("request_id");

        IngestResponse response = IngestResponse.builder()
                .status("success")
                .metricsReceived(request.getMetrics().size())
                .processingTimeMs(processingTime)
                .requestId(requestId)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MetricResponse> createMetric(@Valid @RequestBody CreateMetricRequest request) {
        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        MetricResponse response = metricIngestionService.createSingleMetric(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<MetricResponse>> listMetrics(
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        int validSize = Math.min(Math.max(size, 1), 500);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, validSize, sort);

        PageResponse<MetricResponse> response = metricQueryService.listMetrics(
                tenantId, metricName, startTime, endTime, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetricResponse> getMetricById(@PathVariable Long id) {
        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        MetricResponse response = metricQueryService.getMetricById(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetricResponse> updateMetric(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMetricRequest request) {

        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        MetricResponse response = metricQueryService.updateMetric(tenantId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetric(@PathVariable Long id) {
        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        metricQueryService.deleteMetric(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteMetrics(
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        int deletedCount = metricQueryService.deleteMetrics(tenantId, metricName, startTime, endTime);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "deleted_count", deletedCount
        ));
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> queryMetrics(@Valid @RequestBody QueryRequest request) {
        Tenant tenant = TenantContext.getTenant();
        Long tenantId = tenant.getId();

        QueryResponse response = metricQueryService.query(tenantId, request);
        return ResponseEntity.ok(response);
    }
}