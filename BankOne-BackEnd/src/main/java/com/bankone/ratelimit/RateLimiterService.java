package com.bankone.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Asks Redis-backed buckets: "can this key spend 1 token?"
 * Only active when app.rate-limit.redis-enabled=true (needs ProxyManager bean).
 */
@Service
@ConditionalOnProperty(prefix = "app.rate-limit", name = "redis-enabled", havingValue = "true")
public class RateLimiterService {

    public enum Policy {
        LOGIN,
        API
    }

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties properties;

    public RateLimiterService(ProxyManager<String> proxyManager, RateLimitProperties properties) {
        this.proxyManager = proxyManager;
        this.properties = properties;
    }

    /**
     * @return probe with isConsumed() true/false and remaining tokens / wait time
     */
    public ConsumptionProbe tryConsume(Policy policy, String clientKey) {
        String redisKey = switch (policy) {
            case LOGIN -> "rl:login:" + clientKey;
            case API -> "rl:api:" + clientKey;
        };

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(limitFor(policy))
                .build();

        return proxyManager.builder()
                .build(redisKey, configSupplier)
                .tryConsumeAndReturnRemaining(1);
    }

    private Bandwidth limitFor(Policy policy) {
        int perMinute = switch (policy) {
            case LOGIN -> properties.getLoginPerMinute();
            case API -> properties.getApiPerMinute();
        };
        // capacity = burst size; refillGreedy = refill that many every minute
        return Bandwidth.builder()
                .capacity(perMinute)
                .refillGreedy(perMinute, Duration.ofMinutes(1))
                .build();
    }
}