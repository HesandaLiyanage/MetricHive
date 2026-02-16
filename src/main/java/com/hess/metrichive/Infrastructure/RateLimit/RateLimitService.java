package com.hess.metrichive.Infrastructure.RateLimit;

import io.github.bucket4j.Bandwidth;
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
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder() // Explicitly use Bandwidth builder
                        .capacity(50)
                        .refillGreedy(50, Duration.ofMinutes(1))
                        .build())
                .build();

        // The ProxyManager builds or retrieves the bucket from Redis
        return proxyManager.builder().build(key, configSupplier);
    }
}