package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.services.ScrapedStandingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "f1.api.base-url=http://localhost:8080")
class ScrapedStandingTest {

    @Autowired
    private ScrapedStandingService scrapedStandingService;

    @Test
    @Transactional
    @Commit
    void update2026PointsOnly() {
        System.out.println("--- Starting Targeted 2026 Points Scrape ---");
        
        // This will trigger the scrape for the 2026 results page
        scrapedStandingService.updateDriverAndPointsFromScraperForSeason("2026");
        
        System.out.println("--- Points Scrape Finished ---");
    }
}