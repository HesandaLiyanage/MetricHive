package com.hess.metrichive.Config;


import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

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

}
