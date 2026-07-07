package be.michielve.f1_api.services;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheEvictionService {

    private static final Logger logger = LoggerFactory.getLogger(CacheEvictionService.class);

    private final CacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;

    public CacheEvictionService(CacheManager cacheManager, StringRedisTemplate redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    public void evictAndStamp(String... cacheNames) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        for (String name : cacheNames) {
            Cache cache = cacheManager.getCache(name);
            if (cache == null) {
                logger.warn("Cache '{}' not found — check CacheConfig registration", name);
                continue;
            }
            cache.clear();
            redisTemplate.opsForValue().set("dataset:" + name + ":lastUpdated", timestamp);
            logger.info("Evicted cache '{}', stamped lastUpdated={}", name, timestamp);
        }
    }
}
