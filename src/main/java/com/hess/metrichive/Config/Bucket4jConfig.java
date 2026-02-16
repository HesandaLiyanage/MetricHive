package com.hess.metrichive.Config;




import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class Bucket4jConfig {
    public StatefulRedisConnection<String, byte[]> redisConnection(
            RedisConnectionFactory connectionFactory) {

        // get the Lettuce client from Spring's connection factory
        LettuceConnectionFactory lettuceFactory =
                (LettuceConnectionFactory) connectionFactory;

        RedisClient redisClient = (RedisClient) lettuceFactory.getNativeClient();

        return redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );
    }

    @Bean
    public ProxyManager<String> bucketProxyManager(
            StatefulRedisConnection<String, byte[]> redisConnection) {

        return LettuceBasedProxyManager
                .builderFor(redisConnection)
                .withExpirationAfterWriteStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                Duration.ofMinutes(1)
                        )
                )
                .build();
        // ProxyManager is the thing that creates/manages buckets in Redis
        // one bucket per API key, all stored in Redis
    }

}
