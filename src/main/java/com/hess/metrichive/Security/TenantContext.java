package com.hess.metrichive.Security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {
    // ThreadLocal ensures that each concurrent HTTP request gets its own isolated variable
    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    public static Long getTenantId() {
        return currentTenantId.get();
    }

    // CRITICAL: This prevents massive security leaks
    public static void clear() {
        currentTenantId.remove();
    }
}