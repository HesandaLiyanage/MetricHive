package com.hess.metrichive.Service;

import com.hess.metrichive.Analytics.Security.ApiKeyInventory;
import com.hess.metrichive.Exception.DuplicateResourceException;
import com.hess.metrichive.Exception.ResourceNotFoundException;
import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.MetricJpaRepository;
import com.hess.metrichive.Repository.TenantRepository;
import com.hess.metrichive.dto.CreateTenantRequest;
import com.hess.metrichive.dto.PageResponse;
import com.hess.metrichive.dto.TenantResponse;
import com.hess.metrichive.dto.UpdateTenantRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final MetricJpaRepository metricJpaRepository;
    private final ApiKeyInventory apiKeyInventory;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("Creating new tenant with email: {}", request.getEmail());

        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Tenant already exists with email: " + request.getEmail());
        }

        String apiKey = request.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = generateUniqueApiKey();
        } else if (tenantRepository.existsByApiKey(apiKey)) {
            throw new DuplicateResourceException("API Key is already in use.");
        }

        String tier = (request.getTier() != null && !request.getTier().isBlank())
                ? request.getTier().toLowerCase()
                : "free";

        int defaultMaxMetrics = getDefaultMaxMetricsForTier(tier);
        int defaultMaxRpm = getDefaultMaxRpmForTier(tier);

        Tenant tenant = Tenant.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .apiKey(apiKey)
                .tier(tier)
                .maxMetricsPerDay(request.getMaxMetricsPerDay() != null ? request.getMaxMetricsPerDay() : defaultMaxMetrics)
                .maxRequestsPerMinute(request.getMaxRequestsPerMinute() != null ? request.getMaxRequestsPerMinute() : defaultMaxRpm)
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        apiKeyInventory.addKey(savedTenant.getApiKey());
        log.info("Tenant created successfully with id: {}", savedTenant.getId());

        return TenantResponse.from(savedTenant);
    }

    @Cacheable(value = "tenant", key = "#id")
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return TenantResponse.from(tenant);
    }

    @Cacheable(value = "api", key = "#apiKey")
    @Transactional(readOnly = true)
    public Optional<Tenant> findApiKey(String apiKey) {
        log.info("Validating API key against repository");
        return tenantRepository.findByApiKey(apiKey);
    }

    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> getAllTenants(Pageable pageable) {
        Page<Tenant> page = tenantRepository.findAll(pageable);
        return PageResponse.from(page.map(TenantResponse::from));
    }

    @Caching(evict = {
            @CacheEvict(value = "tenant", key = "#id"),
            @CacheEvict(value = "api", allEntries = true)
    })
    @Transactional
    public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            tenant.setName(request.getName().trim());
        }

        if (request.getTier() != null && !request.getTier().isBlank()) {
            String newTier = request.getTier().toLowerCase();
            tenant.setTier(newTier);
            if (request.getMaxMetricsPerDay() == null) {
                tenant.setMaxMetricsPerDay(getDefaultMaxMetricsForTier(newTier));
            }
            if (request.getMaxRequestsPerMinute() == null) {
                tenant.setMaxRequestsPerMinute(getDefaultMaxRpmForTier(newTier));
            }
        }

        if (request.getMaxMetricsPerDay() != null) {
            tenant.setMaxMetricsPerDay(request.getMaxMetricsPerDay());
        }

        if (request.getMaxRequestsPerMinute() != null) {
            tenant.setMaxRequestsPerMinute(request.getMaxRequestsPerMinute());
        }

        Tenant updated = tenantRepository.save(tenant);
        log.info("Tenant updated successfully with id: {}", id);
        return TenantResponse.from(updated);
    }

    @Caching(evict = {
            @CacheEvict(value = "tenant", key = "#id"),
            @CacheEvict(value = "api", allEntries = true)
    })
    @Transactional
    public void deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tenant not found with id: " + id);
        }

        metricJpaRepository.deleteByTenantId(id);
        tenantRepository.deleteById(id);
        log.info("Tenant and related metrics deleted for id: {}", id);
    }

    @Caching(evict = {
            @CacheEvict(value = "tenant", key = "#id"),
            @CacheEvict(value = "api", allEntries = true)
    })
    @Transactional
    public TenantResponse regenerateApiKey(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        String newKey = generateUniqueApiKey();
        tenant.setApiKey(newKey);

        Tenant updated = tenantRepository.save(tenant);
        apiKeyInventory.addKey(newKey);
        log.info("API key regenerated for tenant id: {}", id);
        return TenantResponse.from(updated);
    }

    private String generateUniqueApiKey() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return "mh_live_" + HexFormat.of().formatHex(bytes);
    }

    private int getDefaultMaxMetricsForTier(String tier) {
        return switch (tier.toLowerCase()) {
            case "pro" -> 50000;
            case "premium" -> 100000;
            case "basic" -> 25000;
            default -> 10000;
        };
    }

    private int getDefaultMaxRpmForTier(String tier) {
        return switch (tier.toLowerCase()) {
            case "pro" -> 500;
            case "premium" -> 1000;
            case "basic" -> 150;
            default -> 60;
        };
    }
}
