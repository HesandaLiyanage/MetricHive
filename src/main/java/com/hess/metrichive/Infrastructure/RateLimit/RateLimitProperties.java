package com.hess.metrichive.Infrastructure.RateLimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "metrichive.rate-limits")
public class RateLimitProperties {

    private Map<String, PlanConfig> plans;

    public Map<String, PlanConfig> getPlans() { return plans; }
    public void setPlans(Map<String, PlanConfig> plans) { this.plans = plans; }

    public static class PlanConfig {
        private long capacity;
        private long refillTokens;
        private Duration refillDuration;

        public long getCapacity() { return capacity; }
        public long getRefillTokens() { return refillTokens; }
        public Duration getRefillDuration() { return refillDuration; }

        public void setCapacity(long capacity) { this.capacity = capacity; }
        public void setRefillTokens(long refillTokens) { this.refillTokens = refillTokens; }
        public void setRefillDuration(Duration refillDuration) { this.refillDuration = refillDuration; }
    }
}