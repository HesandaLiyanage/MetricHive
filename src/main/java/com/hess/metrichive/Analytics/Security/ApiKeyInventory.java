package com.hess.metrichive.Analytics.Security;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ApiKeyInventory {

    private BloomFilter<String> bloomFilter;

    // Parameters for 1 Million keys with 1% false-positive rate
    private final int expectedInsertions = 1000000;
    private final double fpp = 0.01;

    @PostConstruct
    public void init() {
        // Initialize the filter structure
        this.bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                fpp);

        // TODO: Call your Database Service here to load existing keys
        // Example:
        // List<String> keys = apiKeyService.findAllActiveKeys();
        // keys.forEach(this::addKey);
    }

    public boolean mightContain(String apiKey) {
        if (apiKey == null) return false;
        return bloomFilter.mightContain(apiKey);
    }

    public void addKey(String apiKey) {
        bloomFilter.put(apiKey);
    }
}