package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.repositories.RaceRepository;
import be.michielve.f1_api.services.F1Scheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RaceUpdateTest {

    @Autowired
    private F1Scheduler f1Scheduler;
    @Autowired
    private RaceRepository raceRepository;

    @BeforeAll
    static void setVars() {
        DotenvInitializer.init();
    }

    @Test
    public void testRaceUpdate() {
        f1Scheduler.updateRacesSeason();

        List<Race> races = raceRepository.findAllRacesBySeasonName(String.valueOf(LocalDate.now().getYear()));
        assertFalse(races.isEmpty(), "Races should be fetched and saved to the database");
        assertTrue(races.size() > 10, "There should be more than 10 races in a season"); // Example assertion
        assertNotNull(races.get(0).getName(), "Race name should not be null");
    }
}
