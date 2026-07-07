package be.michielve.f1_api.config.resolvers;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class ResultsCacheResolver extends SeasonAwareCacheResolver {
    public ResultsCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String activeCacheName() { return "results"; }

    @Override
    protected String historicalCacheName() { return "results_hist"; }
}