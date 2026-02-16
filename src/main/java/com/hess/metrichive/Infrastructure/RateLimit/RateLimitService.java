package com.hess.metrichive.Infrastructure.RateLimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private final ProxyManager<String> proxyManager;

    public RateLimitService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String key) {
        // Blueprint: 50 requests per minute.
        // RefillGreedy adds tokens linearly (approx. 1 token every 1.2s)
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(50).refillGreedy(50, Duration.ofMinutes(1)))
                .build();

        // Thread-safe: connects to Redis, uses Lua script to check/update state
        return proxyManager.builder().build(key, configSupplier);
    }
}