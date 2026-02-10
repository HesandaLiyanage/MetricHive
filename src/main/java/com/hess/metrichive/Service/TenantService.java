package com.hess.metrichive.Service;


import com.hess.metrichive.Model.Tenant;
import com.hess.metrichive.Repository.TenantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TenantService {
    public final TenantRepository tenantRepository;

    public Optional<Tenant> 
}
