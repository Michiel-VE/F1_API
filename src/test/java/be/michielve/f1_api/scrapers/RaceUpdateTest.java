package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.repositories.RaceRepository;
import be.michielve.f1_api.services.ScrapedRaceService;
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
public class RaceUpdateTest extends BaseTestRecording {

    @Autowired
    private ScrapedRaceService scrapedRaceService;
    
    @Autowired
    private RaceRepository raceRepository;

    private final String year = "2020";

    @Test
    public void testRaceUpdate() {
        // Pass both the URL pattern and the test logic lambda
        runWithRecording("/en/racing/" + year + ".*", () -> {
            scrapedRaceService.updateRaceFromScraperForSeason(year);
        });

        List<Race> races = raceRepository.findAllRacesBySeasonName(year);
        assertFalse(races.isEmpty(), "Races should be fetched and saved to the database");
    }
}