package com.hess.metrichive.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.MetricJpaRepository;
import com.hess.metrichive.Repository.TenantRepository;
import com.hess.metrichive.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MetricJpaRepository metricJpaRepository;

    private Tenant testTenant;
    private Metric sampleMetric;

    @BeforeEach
    void setUp() {
        metricJpaRepository.deleteAll();
        tenantRepository.deleteAll();

        testTenant = Tenant.builder()
                .name("Metric Test Tenant")
                .email("metric-tenant@metrichive.com")
                .apiKey("metric-test-api-key-99999")
                .tier("pro")
                .maxMetricsPerDay(50000)
                .maxRequestsPerMinute(500)
                .build();
        testTenant = tenantRepository.save(testTenant);

        sampleMetric = Metric.builder()
                .tenantId(testTenant.getId())
                .metricName("system.cpu")
                .value(82.5)
                .timestamp(Instant.now().minus(5, ChronoUnit.MINUTES))
                .tags(Map.of("host", "server-01", "env", "prod"))
                .build();
        sampleMetric = metricJpaRepository.save(sampleMetric);
    }

    @Test
    void createMetric_authenticated_returnsCreated() throws Exception {
        CreateMetricRequest request = CreateMetricRequest.builder()
                .name("app.memory")
                .value(1024.0)
                .timestamp(Instant.now().minus(1, ChronoUnit.MINUTES))
                .tags(Map.of("service", "api-gateway"))
                .build();

        mockMvc.perform(post("/api/v1/metrics")
                        .header("X-API-Key", testTenant.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.metric_name", is("app.memory")))
                .andExpect(jsonPath("$.value", is(1024.0)))
                .andExpect(jsonPath("$.tenant_id", is(testTenant.getId().intValue())));
    }

    @Test
    void ingestBatch_authenticated_returnsOk() throws Exception {
        MetricDTO dto1 = new MetricDTO();
        dto1.setName("network.rx");
        dto1.setValue(500.0);
        dto1.setTimestamp(Instant.now().minus(2, ChronoUnit.MINUTES));
        dto1.setTags(Map.of("interface", "eth0"));

        MetricDTO dto2 = new MetricDTO();
        dto2.setName("network.tx");
        dto2.setValue(250.0);
        dto2.setTimestamp(Instant.now().minus(1, ChronoUnit.MINUTES));
        dto2.setTags(Map.of("interface", "eth0"));

        IngestRequest request = new IngestRequest();
        request.setMetrics(List.of(dto1, dto2));

        mockMvc.perform(post("/api/v1/metrics/ingest")
                        .header("X-API-Key", testTenant.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.metrics_received", is(2)));
    }

    @Test
    void getMetricById_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/" + sampleMetric.getId())
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sampleMetric.getId().intValue())))
                .andExpect(jsonPath("$.metric_name", is("system.cpu")))
                .andExpect(jsonPath("$.value", is(82.5)));
    }

    @Test
    void getMetricById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/999999")
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    void getMetricById_otherTenant_returns404() throws Exception {
        Tenant otherTenant = Tenant.builder()
                .name("Other Tenant")
                .email("other@metrichive.com")
                .apiKey("other-api-key-88888")
                .build();
        otherTenant = tenantRepository.save(otherTenant);

        mockMvc.perform(get("/api/v1/metrics/" + sampleMetric.getId())
                        .header("X-API-Key", otherTenant.getApiKey()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listMetrics_authenticated_returnsPage() throws Exception {
        mockMvc.perform(get("/api/v1/metrics?metricName=system.cpu&page=0&size=10")
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].metric_name", is("system.cpu")));
    }

    @Test
    void updateMetric_authenticated_returnsUpdated() throws Exception {
        UpdateMetricRequest update = UpdateMetricRequest.builder()
                .value(95.0)
                .tags(Map.of("host", "server-01", "env", "staging"))
                .build();

        mockMvc.perform(put("/api/v1/metrics/" + sampleMetric.getId())
                        .header("X-API-Key", testTenant.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(95.0)))
                .andExpect(jsonPath("$.tags.env", is("staging")));
    }

    @Test
    void deleteMetric_authenticated_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/metrics/" + sampleMetric.getId())
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/metrics/" + sampleMetric.getId())
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isNotFound());
    }

    @Test
    void bulkDeleteMetrics_authenticated_returnsCount() throws Exception {
        mockMvc.perform(delete("/api/v1/metrics?metricName=system.cpu")
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.deleted_count", is(1)));
    }

    @Test
    void queryMetrics_authenticated_returnsAggregatedResults() throws Exception {
        Instant now = Instant.now();
        QueryRequest query = new QueryRequest();
        query.setMetricName("system.cpu");
        query.setAggregation("avg");
        query.setInterval("1m");
        query.setStartTime(now.minus(1, ChronoUnit.HOURS));
        query.setEndTime(now.plus(1, ChronoUnit.HOURS));

        mockMvc.perform(post("/api/v1/metrics/query")
                        .header("X-API-Key", testTenant.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metric_name", is("system.cpu")))
                .andExpect(jsonPath("$.aggregation", is("avg")))
                .andExpect(jsonPath("$.results", notNullValue()))
                .andExpect(jsonPath("$.metadata.total_results", greaterThanOrEqualTo(1)));
    }

    @Test
    void metricRequest_withoutApiKey_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricRequest_withInvalidApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/metrics")
                        .header("X-API-Key", "totally-invalid-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_code", is("UNAUTHORIZED")));
    }
}
