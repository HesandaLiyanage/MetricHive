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
import java.time.LocalDateTime;
import java.util.HashMap;
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
        }else {
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

    private void createSampleMetrics(Long id) {
        Metric metric = new Metric();
        metric.setTenantId(id);
        metric.setMetricName("cpu_usage");
        metric.setValue(75.5);
        metric.setTimestamp(LocalDateTime.now().minusMinutes(1));
        Map<String, String> cpuTags = new HashMap<>();
        cpuTags.put("host", "server-01");
        cpuTags.put("region", "us-east");
        metric.setTags(cpuTags);
        metricRepository.save(metric);

        // Memory Usage metrics
        Metric memUsage = new Metric();
        memUsage.setTenantId(id);
        memUsage.setMetricName("memory_usage");
        memUsage.setValue(62.3);
        memUsage.setTimestamp(LocalDateTime.now().minusMinutes(1));
        Map<String, String> memTags = new HashMap<>();
        memTags.put("host", "server-01");
        memTags.put("region", "us-east");
        memUsage.setTags(memTags);
        metricRepository.save(memUsage);

        // HTTP Requests metrics
        Metric httpRequests = new Metric();
        httpRequests.setTenantId(id);
        httpRequests.setMetricName("http_requests_per_second");
        httpRequests.setValue(125.0);
        httpRequests.setTimestamp(LocalDateTime.now());
        Map<String, String> httpTags = new HashMap<>();
        httpTags.put("endpoint", "/api/users");
        httpTags.put("method", "GET");
        httpRequests.setTags(httpTags);
        metricRepository.save(httpRequests);
    }
}