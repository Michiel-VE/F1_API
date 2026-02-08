package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.repositories.RaceRepository;
import be.michielve.f1_api.services.ScrapedRaceService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RaceUpdateTest {

    @Autowired
    private ScrapedRaceService scrapedRaceService;
    @Autowired
    private RaceRepository raceRepository;

    String year = "2020";

    @BeforeAll
    static void setVars() {
        DotenvInitializer.init();
    }

    @Test
    public void testRaceUpdate() {
        scrapedRaceService.updateRaceFromScraperForSeason(year);

        List<Race> races = raceRepository.findAllRacesBySeasonName(year);
        assertFalse(races.isEmpty(), "Races should be fetched and saved to the database");
        assertTrue(races.size() > 10, "There should be more than 10 races in a season");
        assertNotNull(races.get(0).getName(), "Race name should not be null");
    }
}
