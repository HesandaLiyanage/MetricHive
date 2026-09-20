package com.hess.metrichive.Controller;

import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Security.TenantContext;
import com.hess.metrichive.Service.TenantService;
import com.hess.metrichive.dto.CreateTenantRequest;
import com.hess.metrichive.dto.PageResponse;
import com.hess.metrichive.dto.TenantResponse;
import com.hess.metrichive.dto.UpdateTenantRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<TenantResponse> getCurrentTenant() {
        Tenant currentTenant = TenantContext.getTenant();
        if (currentTenant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TenantResponse response = tenantService.getTenantById(currentTenant.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<TenantResponse>> listTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        int validSize = Math.min(Math.max(size, 1), 100);
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, validSize, sort);

        PageResponse<TenantResponse> response = tenantService.getAllTenants(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long id) {
        TenantResponse response = tenantService.getTenantById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/regenerate-key")
    public ResponseEntity<TenantResponse> regenerateApiKey(@PathVariable Long id) {
        TenantResponse response = tenantService.regenerateApiKey(id);
        return ResponseEntity.ok(response);
    }
}
