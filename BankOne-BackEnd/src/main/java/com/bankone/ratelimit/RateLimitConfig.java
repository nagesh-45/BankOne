package com.bankone.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Creates Bucket4j's Redis ProxyManager only when
 * app.rate-limit.redis-enabled=true.
 * If false, these beans are absent → app starts without Redis.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = "app.rate-limit", name = "redis-enabled", havingValue = "true")
public class RateLimitConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port
    ) {
        return RedisClient.create(RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> rateLimitRedisConnection(RedisClient rateLimitRedisClient) {
        return rateLimitRedisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );
    }

    /**
     * ProxyManager = "give me a bucket for this key, backed by Redis".
     * CAS = compare-and-swap so concurrent requests update safely.
     */
    @Bean
    public ProxyManager<String> rateLimitProxyManager(
            StatefulRedisConnection<String, byte[]> rateLimitRedisConnection
    ) {
        return LettuceBasedProxyManager.builderFor(rateLimitRedisConnection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                Duration.ofMinutes(2)
                        )
                )
                .build();
    }
}