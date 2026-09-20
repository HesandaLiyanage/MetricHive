package com.hess.metrichive.Config;

import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AbstractCompareAndSwapBasedProxyManager;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AsyncCompareAndSwapOperation;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.CompareAndSwapOperation;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class RedisRateLimitConfig {

    @Bean
    public RedisClient redisClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        return RedisClient.create(String.format("redis://%s:%d", host, port));
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        try {
            return LettuceBasedProxyManager.builderFor(redisClient).build();
        } catch (Exception e) {
            log.warn("Unable to initialize Redis-backed ProxyManager ({}). Falling back to in-memory ProxyManager.", e.getMessage());
            return new InMemoryStringProxyManager().withMapper(bytes -> new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static class InMemoryStringProxyManager extends AbstractCompareAndSwapBasedProxyManager<String> {
        private final ConcurrentHashMap<String, byte[]> stateMap = new ConcurrentHashMap<>();

        InMemoryStringProxyManager() {
            super(ClientSideConfig.getDefault());
        }

        @Override
        protected CompareAndSwapOperation beginCompareAndSwapOperation(String key) {
            return new CompareAndSwapOperation() {
                @Override
                public Optional<byte[]> getStateData(Optional<Long> timeout) {
                    return Optional.ofNullable(stateMap.get(key));
                }

                @Override
                public boolean compareAndSwap(byte[] originalData, byte[] newData, RemoteBucketState state, Optional<Long> timeout) {
                    if (originalData == null) {
                        return stateMap.putIfAbsent(key, newData) == null;
                    } else {
                        return stateMap.replace(key, originalData, newData);
                    }
                }
            };
        }

        @Override
        protected AsyncCompareAndSwapOperation beginAsyncCompareAndSwapOperation(String key) {
            throw new UnsupportedOperationException("Async mode not supported in fallback proxy manager");
        }

        @Override
        public boolean isAsyncModeSupported() {
            return false;
        }

        @Override
        public void removeProxy(String key) {
            stateMap.remove(key);
        }

        @Override
        protected CompletableFuture<Void> removeAsync(String key) {
            stateMap.remove(key);
            return CompletableFuture.completedFuture(null);
        }
    }
}