package com.hess.metrichive.Infrastructure.RateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;
    private final RateLimitProperties properties;

    public RateLimitService(LettuceBasedProxyManager<String> proxyManager,
                            RateLimitProperties properties) {
        this.proxyManager = proxyManager;
        this.properties = properties;
    }

    // key      = unique identifier (e.g. userId or IP)
    // planType = "free", "basic", "pro"
    public Bucket resolveBucket(String key, String planType) {
        Map<String, RateLimitProperties.PlanConfig> plans = properties.getPlans();

        // Fallback to "free" if the planType is unrecognized
        RateLimitProperties.PlanConfig plan = plans.getOrDefault(
                planType.toLowerCase(),
                plans.get("free")
        );

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(plan.getCapacity())
                        .refillGreedy(plan.getRefillTokens(), plan.getRefillDuration())
                        .build())
                .build();

        return proxyManager.builder().build(key, configSupplier);
    }
}