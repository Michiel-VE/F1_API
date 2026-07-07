package be.michielve.f1_api.config.resolvers;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.time.Year;
import java.util.Collection;
import java.util.Collections;

public abstract class SeasonAwareCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;

    protected SeasonAwareCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected abstract String activeCacheName();
    protected abstract String historicalCacheName();

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        String season = extractSeason(context.getArgs());
        boolean isCurrentSeason = season != null
                && season.equals(String.valueOf(Year.now().getValue()));

        String cacheName = isCurrentSeason ? activeCacheName() : historicalCacheName();
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("No cache registered for name: " + cacheName
                    + " — check CacheConfig");
        }
        return Collections.singletonList(cache);
    }

    private String extractSeason(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String s) {
                return s;
            }
        }
        return null;
    }
}