package com.hess.metrichive.Security;

import com.hess.metrichive.Model.Tenant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {
    private static final ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();

    public static void setTenant(Tenant tenant) {
        currentTenant.set(tenant);
    }

    public static Tenant getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}