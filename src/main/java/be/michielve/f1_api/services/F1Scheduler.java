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

    private final String currentYear = String.valueOf(Year.now().getValue());

    public void updateRacesSeason() {
        scrapedRaceService.updateRaceFromScraperForSeason(currentYear);
    }

    public void updateCurrentDriversAndPoints() {
        scrapedStandingService.updateDriverAndPointsFromScraperForSeason(currentYear);
    }

    public void updateDriverAndTeam() {
        scrapedDriverService.updateDriversFromScraper();
        scrapedTeamService.updateTeamsFromScraper();
    }

    public void updateRaceResults() {
        scrapedRaceResultService.updatePendingRaceResults(currentYear);
    }
}