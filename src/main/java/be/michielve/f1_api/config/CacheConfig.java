package be.michielve.f1_api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializationContext.SerializationPair<Object> jsonSerializer = RedisSerializationContext.SerializationPair
                .fromSerializer(RedisSerializer.json());

        RedisCacheConfiguration activeConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(5))
                .serializeValuesWith(jsonSerializer)
                .computePrefixWith(cacheName -> cacheName + "::");

        RedisCacheConfiguration historicalConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(365))
                .serializeValuesWith(jsonSerializer)
                .computePrefixWith(cacheName -> cacheName + "::");

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("drivers", activeConfig);
        cacheConfigs.put("drivers_hist", historicalConfig);
        cacheConfigs.put("teams", activeConfig);
        cacheConfigs.put("teams_hist", historicalConfig);
        cacheConfigs.put("races", activeConfig);
        cacheConfigs.put("races_hist", historicalConfig);
        cacheConfigs.put("results", activeConfig);
        cacheConfigs.put("results_hist", historicalConfig);

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(
                connectionFactory, BatchStrategies.scan(100));

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(activeConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}