package com.hess.metrichive.Infrastructure.RateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Service
@ConfigurationProperties(prefix = "metrichive") // (Line 1) Maps YAML 'metrichive' prefix to this class
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    // (Line 2) This map will hold our plan details: "free", "basic", "pro"
    private Map<String, PlanConfig> rateLimits;

    public RateLimitService(LettuceBasedProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    // (Line 3) Helper class to match the structure in application.yml
    public static class PlanConfig {
        public long capacity;
        public long refillTokens;
        public Duration refillDuration;
        // Getters/Setters required for @ConfigurationProperties
        public void setCapacity(long c) { this.capacity = c; }
        public void setRefillTokens(long r) { this.refillTokens = r; }
        public void setRefillDuration(Duration d) { this.refillDuration = d; }
    }

    public Bucket resolveBucket(String key, String planType) {
        // (Line 4) Lookup plan details from the YAML map. Fallback to 'free' if missing.
        PlanConfig plan = rateLimits.getOrDefault(planType.toLowerCase(), rateLimits.get("free"));

        // (Line 5) Create the blueprint based on the selected plan's data
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(plan.capacity)
                        .refillGreedy(plan.refillTokens, plan.refillDuration)
                        .build())
                .build();

        // (Line 6) The ProxyManager handles the Redis connection and Lua script execution
        return proxyManager.builder().build(key, configSupplier);
    }

    public void setRateLimits(Map<String, PlanConfig> rateLimits) { this.rateLimits = rateLimits; }
}