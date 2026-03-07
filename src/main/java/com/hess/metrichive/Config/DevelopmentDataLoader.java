package com.hess.metrichive.Config;

import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.Repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")  // Only runs in dev profile
@RequiredArgsConstructor
@Slf4j
public class DevelopmentDataLoader implements CommandLineRunner {

    public final MetricRepository metricRepository;
    public final TenantRepository tenantRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Data Loader running");

        if (tenantRepository.count() != 0) {
            log.info("Data already injected to the database. Exiting DataLoader...");
        } else {
            loadSampleData();
        }
    }

    private void loadSampleData() {
        Tenant devTenant = new Tenant();
        devTenant.setName("Development Tenant");
        devTenant.setEmail("dev@example.com");
        devTenant.setApiKey("dev-api-key-123456789");
        devTenant.setTier("premium");
        devTenant.setMaxMetricsPerDay(100000);
        devTenant.setMaxRequestsPerMinute(1000);

        Tenant testTenant = new Tenant();
        testTenant.setName("Test Tenant");
        testTenant.setEmail("test@example.com");
        testTenant.setApiKey("test-api-key-abcdef123");
        testTenant.setTier("free");
        testTenant.setMaxMetricsPerDay(10000);
        testTenant.setMaxRequestsPerMinute(60);

        Tenant stagingTenant = new Tenant();
        stagingTenant.setName("Staging Tenant");
        stagingTenant.setEmail("staging@example.com");
        stagingTenant.setApiKey("stage-api-key-xyz789");
        stagingTenant.setTier("pro");
        stagingTenant.setMaxMetricsPerDay(50000);
        stagingTenant.setMaxRequestsPerMinute(500);

        Tenant savedDevTenant = tenantRepository.save(devTenant);
        Tenant savedTestTenant = tenantRepository.save(testTenant);
        Tenant savedStagingTenant = tenantRepository.save(stagingTenant);

        createSampleMetrics(savedDevTenant.getId());
        createSampleMetrics(savedTestTenant.getId());
        createSampleMetrics(savedStagingTenant.getId());

        log.info("Development data loaded successfully!");
    }

    private void createSampleMetrics(Long tenantId) {
        List<Metric> metricsToBatchInsert = new ArrayList<>();
        Instant now = Instant.now();

        // CPU Usage metric
        Metric metric = new Metric();
        metric.setTenantId(tenantId);
        metric.setMetricName("cpu_usage");
        metric.setValue(75.5);
        metric.setTimestamp(now.minus(1, ChronoUnit.MINUTES)); // Updated to Instant
        Map<String, String> cpuTags = new HashMap<>();
        cpuTags.put("host", "server-01");
        cpuTags.put("region", "us-east");
        metric.setTags(cpuTags);
        metricsToBatchInsert.add(metric); // Add to list instead of saving immediately

        // Memory Usage metric
        Metric memUsage = new Metric();
        memUsage.setTenantId(tenantId);
        memUsage.setMetricName("memory_usage");
        memUsage.setValue(62.3);
        memUsage.setTimestamp(now.minus(1, ChronoUnit.MINUTES)); // Updated to Instant
        Map<String, String> memTags = new HashMap<>();
        memTags.put("host", "server-01");
        memTags.put("region", "us-east");
        memUsage.setTags(memTags);
        metricsToBatchInsert.add(memUsage);.

        // HTTP Requests metric
        Metric httpRequests = new Metric();
        httpRequests.setTenantId(tenantId);
        httpRequests.setMetricName("http_requests_per_second");
        httpRequests.setValue(125.0);
        httpRequests.setTimestamp(now); // Updated to Instant
        Map<String, String> httpTags = new HashMap<>();
        httpTags.put("endpoint", "/api/users");
        httpTags.put("method", "GET");
        httpRequests.setTags(httpTags);
        metricsToBatchInsert.add(httpRequests);

        // Perform the high-performance batch insert
        metricRepository.batchInsert(metricsToBatchInsert);
    }
}