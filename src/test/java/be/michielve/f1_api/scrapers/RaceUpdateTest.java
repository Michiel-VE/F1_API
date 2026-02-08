package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.repositories.RaceRepository;
import be.michielve.f1_api.services.ScrapedRaceService;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "f1.api.base-url=http://localhost:8080")
public class RaceUpdateTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig()
                .port(8080)
                .usingFilesUnderDirectory("src/test/resources/wiremock/races/updateRaceTest"))
            .build();

    @Autowired
    private ScrapedRaceService scrapedRaceService;
    @Autowired
    private RaceRepository raceRepository;

    private final String year = "2020";

    @BeforeAll
    static void setVars() {
        DotenvInitializer.init();
    }

    @Test
    public void testRaceUpdate() {
        wm.stubFor(get(urlMatching("/en/racing/" + year + ".*"))
                .withName("f1-race-data-" + year) 
                .willReturn(aResponse()
                        .proxiedFrom("https://www.formula1.com")));

        scrapedRaceService.updateRaceFromScraperForSeason(year);

        List<Race> races = raceRepository.findAllRacesBySeasonName(year);
        assertFalse(races.isEmpty());

        wm.snapshotRecord();
    }
}