package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.repositories.TeamRepository;
import be.michielve.f1_api.services.ScrapedTeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "f1.api.base-url=http://localhost:8080")
public class ScrapedTeamTest extends BaseTestRecording {

    @Autowired
    private ScrapedTeamService scrapedTeamService;

    @Autowired
    private TeamRepository teamRepository;

    private final String year = "2025";

    @Test
    public void testTeamUpdate() {
        runWithRecording("/en/teams.*", () -> {
            scrapedTeamService.updateTeamsFromScraper();
        });

        List<Team> teams = teamRepository.findAllTeamsBySeasonName(year);
        assertFalse(teams.isEmpty(), "Teams should be fetched and saved to the database");
    }

    @Test
    void runScraperAndSaveToDb() {
        System.out.println("--- Starting Manual Scrape Execution ---");
        
        scrapedTeamService.updateTeamsFromScraper();
        
        System.out.println("--- Scrape Execution Finished ---");
    }
}