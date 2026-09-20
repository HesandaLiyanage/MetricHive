package com.hess.metrichive.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hess.metrichive.Exception.ErrorResponse;
import com.hess.metrichive.Model.Tenant;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Tenant tenant = TenantContext.getTenant();

        if (tenant == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Extract true IP Address
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        // 2. Branch (Per-IP) Limit: 200 requests per minute
        Supplier<BucketConfiguration> ipConfigSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(200)
                        .refillIntervally(200, Duration.ofMinutes(1))
                        .build())
                .build();

        byte[] ipKey = ("rate_limit_tenant_" + tenant.getId() + "_ip_" + clientIp).getBytes();
        Bucket ipBucket = proxyManager.builder().build(ipKey, ipConfigSupplier);

        // 3. Global (Per-Tenant) Limit: Fetched from DB
        Supplier<BucketConfiguration> tenantConfigSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(tenant.getMaxRequestsPerMinute())
                        .refillIntervally(tenant.getMaxRequestsPerMinute(), Duration.ofMinutes(1))
                        .build())
                .build();

        byte[] tenantKey = ("rate_limit_tenant_" + tenant.getId()).getBytes();
        Bucket tenantBucket = proxyManager.builder().build(tenantKey, tenantConfigSupplier);

        // 4. Sequential Bucket Checks with resilient fallback
        try {
            if (!ipBucket.tryConsume(1)) {
                log.warn("IP rate limit exceeded for Tenant ID: {} at IP: {}", tenant.getId(), clientIp);
                sendTooManyRequestsError(response, "Branch IP rate limit exceeded. Limit is 200 req/min.");
                return;
            }

            if (!tenantBucket.tryConsume(1)) {
                log.warn("Global rate limit exceeded for Tenant ID: {}", tenant.getId());
                sendTooManyRequestsError(response, "Global tenant rate limit exceeded.");
                return;
            }
        } catch (Exception e) {
            log.warn("Rate limit check bypassed due to Redis connection issue: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void sendTooManyRequestsError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String reqId = org.slf4j.MDC.get("request_id");
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("RATE_LIMIT_EXCEEDED")
                .message(message)
                .timestamp(Instant.now())
                .requestId(reqId != null ? reqId : "req_unknown")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}