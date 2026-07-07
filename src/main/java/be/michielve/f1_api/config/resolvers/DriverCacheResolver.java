package be.michielve.f1_api.config.resolvers;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class DriverCacheResolver extends SeasonAwareCacheResolver {
    public DriverCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String activeCacheName() { return "drivers"; }

    @Override
    protected String historicalCacheName() { return "drivers_hist"; }
}