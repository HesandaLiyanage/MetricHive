package com.hess.metrichive.Service;


import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TenantService {
    public final TenantRepository tenantRepository;

    public Optional<Tenant> tenantFind(Long Id) {
        Optional<Tenant> tenant = tenantRepository.findById(Id);
        return tenant;
    }

    @Cacheable(value = "api" , key = "#apiKey")
    public Optional<Tenant> findApiKey(String apiKey) {
        log.info("Got a request to validate the api key {}", apiKey);
        Optional<Tenant> tenant = tenantRepository.findByApiKey(apiKey);
        return tenant;
    }

}
