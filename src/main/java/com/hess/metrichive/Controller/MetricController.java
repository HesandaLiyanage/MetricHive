package com.hess.metrichive.Controller;


import com.hess.metrichive.Service.MetricIngestionService;
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

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestMetrics(@Valid @RequestBody)

}
