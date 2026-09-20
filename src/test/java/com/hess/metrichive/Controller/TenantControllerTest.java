package com.hess.metrichive.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.TenantRepository;
import com.hess.metrichive.dto.CreateTenantRequest;
import com.hess.metrichive.dto.UpdateTenantRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        tenantRepository.deleteAll();
        testTenant = Tenant.builder()
                .name("Integration Test Tenant")
                .email("it-tenant@metrichive.com")
                .apiKey("it-test-api-key-12345")
                .tier("pro")
                .maxMetricsPerDay(50000)
                .maxRequestsPerMinute(500)
                .build();
        testTenant = tenantRepository.save(testTenant);
    }

    @Test
    void createTenant_publicEndpoint_returnsCreated() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Acme Corp")
                .email("admin@acme.com")
                .tier("pro")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Acme Corp")))
                .andExpect(jsonPath("$.email", is("admin@acme.com")))
                .andExpect(jsonPath("$.api_key", startsWith("mh_live_")))
                .andExpect(jsonPath("$.tier", is("pro")))
                .andExpect(jsonPath("$.max_metrics_per_day", is(50000)))
                .andExpect(jsonPath("$.max_requests_per_minute", is(500)));
    }

    @Test
    void createTenant_duplicateEmail_returnsConflict() throws Exception {
        CreateTenantRequest duplicate = CreateTenantRequest.builder()
                .name("Duplicate Tenant")
                .email("it-tenant@metrichive.com")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_code", is("CONFLICT")));
    }

    @Test
    void createTenant_invalidEmail_returnsUnprocessableEntity() throws Exception {
        CreateTenantRequest invalid = CreateTenantRequest.builder()
                .name("Invalid Email Tenant")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error_code", is("VALIDATION_FAILED")));
    }

    @Test
    void getCurrentTenant_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/me")
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testTenant.getId().intValue())))
                .andExpect(jsonPath("$.email", is("it-tenant@metrichive.com")));
    }

    @Test
    void getTenantById_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/" + testTenant.getId())
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testTenant.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Integration Test Tenant")));
    }

    @Test
    void getTenantById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/999999")
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    void listTenants_authenticated_returnsPage() throws Exception {
        mockMvc.perform(get("/api/v1/tenants?page=0&size=10")
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.page_number", is(0)));
    }

    @Test
    void updateTenant_authenticated_returnsUpdated() throws Exception {
        UpdateTenantRequest update = UpdateTenantRequest.builder()
                .name("Renamed Tenant")
                .tier("premium")
                .build();

        mockMvc.perform(put("/api/v1/tenants/" + testTenant.getId())
                        .header("X-API-Key", testTenant.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Renamed Tenant")))
                .andExpect(jsonPath("$.tier", is("premium")))
                .andExpect(jsonPath("$.max_metrics_per_day", is(100000)));
    }

    @Test
    void regenerateApiKey_authenticated_returnsNewKey() throws Exception {
        String oldKey = testTenant.getApiKey();

        mockMvc.perform(post("/api/v1/tenants/" + testTenant.getId() + "/regenerate-key")
                        .header("X-API-Key", oldKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.api_key", not(equalTo(oldKey))))
                .andExpect(jsonPath("$.api_key", startsWith("mh_live_")));
    }

    @Test
    void deleteTenant_authenticated_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/tenants/" + testTenant.getId())
                        .header("X-API-Key", testTenant.getApiKey()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tenants/" + testTenant.getId())
                        .header("X-API-Key", "it-test-api-key-12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessWithoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/me"))
                .andExpect(status().isForbidden());
    }
}
