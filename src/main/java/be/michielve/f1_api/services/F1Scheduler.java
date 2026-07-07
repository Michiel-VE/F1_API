package be.michielve.f1_api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class F1Scheduler {

    private final ScrapedStandingService scrapedStandingService;
    private final ScrapedRaceService scrapedRaceService;
    private final ScrapedDriverService scrapedDriverService;
    private final ScrapedTeamService scrapedTeamService;
    private final ScrapedRaceResultService scrapedRaceResultService;
    private final CacheEvictionService cacheEvictionService;

    private final String currentYear = String.valueOf(Year.now().getValue());

    public void updateRacesSeason() {
        scrapedRaceService.updateRaceFromScraperForSeason(currentYear);
        cacheEvictionService.evictAndStamp("races");
    }

    public void updateCurrentDriversAndPoints() {
        scrapedStandingService.updateDriverAndPointsFromScraperForSeason(currentYear);
        cacheEvictionService.evictAndStamp("drivers", "results");
    }

    public void updateDriverAndTeam() {
        scrapedDriverService.updateDriversFromScraper();
        scrapedTeamService.updateTeamsFromScraper();
        
        cacheEvictionService.evictAndStamp("drivers", "teams");
    }

    public void updateRaceResults() {
        scrapedRaceResultService.updatePendingRaceResults(currentYear);
        cacheEvictionService.evictAndStamp("results");
    }
}