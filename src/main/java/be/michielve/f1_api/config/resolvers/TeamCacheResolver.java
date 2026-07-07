package be.michielve.f1_api.config.resolvers;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class TeamCacheResolver extends SeasonAwareCacheResolver {
    public TeamCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String activeCacheName() { return "teams"; }

    @Override
    protected String historicalCacheName() { return "teams_hist"; }
}
