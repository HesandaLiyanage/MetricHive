package com.hess.metrichive.Security;

import com.hess.metrichive.Model.Tenant;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();
    private static final ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    public static void setTenant(Tenant tenant) {
        currentTenant.set(tenant);
        if (tenant != null) {
            currentTenantId.set(tenant.getId());
        }
    }

    public static void setUserId(String userId) {
        currentUserId.set(userId);
    }

    public static Long getTenantId() {
        Long tenantId = currentTenantId.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return tenantId;
    }

    public static Tenant getTenant() {
        Tenant tenant = currentTenant.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return tenant;
    }

    public static String getUserId() {
        return currentUserId.get();
    }

    /**
     * CRITICAL: Always call this in finally block!
     * Spring reuses threads - without this, tenant A's data leaks to tenant B
     */
    public static void clear() {
        currentTenantId.remove();
        currentTenant.remove();
        currentUserId.remove();
    }
}