package be.michielve.f1_api.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheManagerService {

    private static final Logger logger = LoggerFactory.getLogger(CacheManagerService.class);

    private static final List<String> ALL_CACHE_NAMES = List.of(
            "drivers", "drivers_hist",
            "teams", "teams_hist",
            "races", "races_hist",
            "results", "results_hist"
    );

    private final CacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;

    /**
     * Clears an entire cache region (both current-season and historical
     * entries within it). Prefer CacheEvictionService.evictAndStamp for
     * the routine post-scrape eviction path — this is for ad hoc/manual use.
     */
    public void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("Cache '{}' not found — check CacheConfig registration", cacheName);
            return;
        }
        cache.clear();
        logger.info("Cleared entire cache region '{}'", cacheName);
    }

    /**
     * Evicts only entries for a specific season, across every cache region
     * that could hold it (active + historical), without touching other
     * seasons' entries in the same region. Uses non-blocking SCAN + UNLINK,
     * never KEYS/DEL, so it doesn't stall the live API under load.
     */
    public void evictSeasonAcrossAllCaches(String season) {
        for (String cacheName : ALL_CACHE_NAMES) {
            String pattern = cacheName + "::season:" + season + "*";
            long removed = deleteKeysMatching(pattern);
            if (removed > 0) {
                logger.info("Evicted {} key(s) matching '{}'", removed, pattern);
            }
        }
    }

    private long deleteKeysMatching(String pattern) {
        List<String> keysToDelete = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .keyCommands()
                .scan(options)) {
            while (cursor.hasNext()) {
                keysToDelete.add(new String(cursor.next()));
            }
        }

        if (keysToDelete.isEmpty()) {
            return 0;
        }
        redisTemplate.unlink(keysToDelete);
        return keysToDelete.size();
    }
}