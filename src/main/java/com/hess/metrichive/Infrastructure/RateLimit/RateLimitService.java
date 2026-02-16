package com.hess.metrichive.Infrastructure.RateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    public RateLimitService(LettuceBasedProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String key) {
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(50)
                        .refillGreedy(50, Duration.ofMinutes(1))
                        .build())
                .build();

        // Use the proxyManager instance to build the remote bucket
        return proxyManager.builder().build(key, configSupplier);
    }
}