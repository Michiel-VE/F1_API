package be.michielve.f1_api.config.resolvers;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class RaceCacheResolver extends SeasonAwareCacheResolver {
    public RaceCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String activeCacheName() { return "races"; }

    @Override
    protected String historicalCacheName() { return "races_hist"; }
}