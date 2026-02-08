package be.michielve.f1_api.scrapers;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.repositories.TeamRepository;
import be.michielve.f1_api.services.ScrapedTeamService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ScrapedTeamTest {
    @Autowired
    private ScrapedTeamService scrapedTeamService;
    @Autowired
    private TeamRepository teamRepository;

    String year = "2025";

    @BeforeAll
    static void setVars() {
        DotenvInitializer.init();
    }

    @Test
    public void testTeamUpdate() {
        scrapedTeamService.updateTeamsFromScraper();

        List<Team> teams = teamRepository.findAllTeamsBySeasonName(year);

        assertFalse(teams.isEmpty(), "Teams should be fetched and saved to the database");

    }
}