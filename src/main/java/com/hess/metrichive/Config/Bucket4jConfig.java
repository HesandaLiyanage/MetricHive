package com.hess.metrichive.Config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class Bucket4jConfig {

    @Bean
    public ProxyManager<String> bucketProxyManager(RedisConnectionFactory connectionFactory) {
        // Grab the native Lettuce client from Spring's default Factory
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        RedisClient redisClient = (RedisClient) lettuceFactory.getNativeClient();

        // Modern 8.x+ approach for Lettuce ProxyManager
        return Bucket4jLettuce.casBasedBuilder(redisClient)
                .expirationAfterWrite(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(2))
                )
                .build();
    }
}