package com.bankone.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache-aside via Spring Cache + Redis. Active only when
 * {@code app.cache.redis-enabled=true} (local). Prod leaves this off —
 * {@code @Cacheable} then becomes a no-op (no CacheManager / EnableCaching).
 *
 * <p>Values use JDK serialization so {@code Page}/{@code List} (search results)
 * round-trip reliably. Jackson default-typing failed on root collections after search.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "app.cache", name = "redis-enabled", havingValue = "true")
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            CacheProperties properties
    ) {
        RedisSerializer<Object> valueSerializer = RedisSerializer.java();

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("bankone:")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer))
                .entryTtl(Duration.ofSeconds(Math.max(1, properties.getTtlSeconds())))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CacheNames.CUSTOMERS, defaults.entryTtl(Duration.ofMinutes(5)));
        perCache.put(CacheNames.ACCOUNTS, defaults.entryTtl(Duration.ofMinutes(2)));
        perCache.put(CacheNames.POLICIES, defaults.entryTtl(Duration.ofMinutes(30)));
        perCache.put(CacheNames.ROLES, defaults.entryTtl(Duration.ofMinutes(30)));
        perCache.put(CacheNames.USERS, defaults.entryTtl(Duration.ofMinutes(5)));
        perCache.put(CacheNames.TRANSACTIONS, defaults.entryTtl(Duration.ofSeconds(60)));
        perCache.put(CacheNames.BENEFICIARIES, defaults.entryTtl(Duration.ofMinutes(5)));
        perCache.put(CacheNames.DASHBOARD, defaults.entryTtl(Duration.ofSeconds(60)));
        perCache.put(CacheNames.TRANSFERS, defaults.entryTtl(Duration.ofSeconds(30)));
        perCache.put(CacheNames.REPORTS, defaults.entryTtl(Duration.ofSeconds(60)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
