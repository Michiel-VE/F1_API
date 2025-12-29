package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.TeamRepository;
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
public class DriverAndTeamUpdateTest {

    @Autowired
    private F1Scheduler f1Scheduler;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private TeamRepository teamRepository;

    @BeforeAll
    static void setVars() {
        DotenvInitializer.init();
    }


    @Test
    public void testDriverUpdate() {
        f1Scheduler.updateDriverAndTeam();

        List<Driver> drivers = driverRepository.findAllBySeasonName(String.valueOf(LocalDate.now().getYear()));

        assertFalse(drivers.isEmpty(), "Drivers should be fetched and saved to the database");
        assertTrue(drivers.size() > 10, "There should be more than 10 races in a season");
        assertNotNull(drivers.get(0).getDriverCode(), "DriverCode name should not be null");
        assertNotNull(drivers.get(5).getLastname(), "Lastname name should not be null");
        assertNotNull(drivers.get(2).getFirstname(), "Firstname name should not be null");
        assertNotNull(drivers.get(8).getBirthday(), "Birthday name should not be null");
        assertNotNull(drivers.get(8).getCountry(), "Country name should not be null");
        assertNotNull(drivers.get(18).getPermanentNumber(), "PermanentNumber name should not be null");
    }

    @Test
    public void testTeamUpdate() {
        f1Scheduler.updateDriverAndTeam();

        List<Team> teams = teamRepository.findAllTeamsBySeasonName(String.valueOf(LocalDate.now().getYear()));

        assertFalse(teams.isEmpty(), "Teams should be fetched and saved to the database");
        assertTrue(teams.size() >= 10 && teams.size() <= 12, "The number of teams should be between 10 and 12, inclusive.");
        assertNotNull(teams.get(0).getName(), "Name name should not be null");
        assertNotNull(teams.get(5).getCountry(), "Country name should not be null");
        assertNotNull(teams.get(4).getShortName(), "Shortname name should not be null");
        assertNotNull(teams.get(9).getBase(), "Base name should not be null");
    }
}
