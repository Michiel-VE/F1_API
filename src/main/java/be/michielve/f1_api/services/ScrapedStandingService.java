package be.michielve.f1_api.services;

import be.michielve.f1_api.models.*;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.DriverTeamSeasonRepository;
import be.michielve.f1_api.repositories.SeasonRepository;
import be.michielve.f1_api.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ScrapedStandingService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedStandingService.class);

    private final DriverRepository driverRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final DriverTeamSeasonRepository driverTeamSeasonRepository;

    @Autowired
    @Lazy
    private ScrapedStandingService self;

    private String TABLE_BODY_CONTENT_SELECTOR = "table tbody";

    public List<ScrapedStanding> scrapeF1Standings(String year) {
        List<ScrapedStanding> scrapedStandings = new ArrayList<>();
        logger.info("Starting to scrape F1 standings for year {}", year);

        try {
            String url = "https://www.formula1.com/en/results/" + year + "/drivers";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            Element tableBody = doc.selectFirst(TABLE_BODY_CONTENT_SELECTOR);
            if (tableBody == null) {
                logger.warn("No standings table found on page for year {}. Scrape failed.", year);
                return scrapedStandings;
            }

            Elements rows = tableBody.select("tr");
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() >= 5) {
                    ScrapedStanding standing = new ScrapedStanding();
                    standing.setPosition(Integer.parseInt(cells.get(0).text().trim()));
                    String[] nameData = cells.get(1).text().trim().split(" ");
                    standing.setFirstName(nameData[0]);

                    if (nameData.length > 1) {
                        int lastIndex = nameData.length - 1;
                        String lastElement = nameData[lastIndex];
                        String driverCode;
                        String lastName;
                        int nameEndIndex = lastIndex;

                        if (lastElement.length() == 3 && lastElement.matches("[A-Z]{3}") && lastIndex > 0) {
                            driverCode = lastElement;
                            nameEndIndex = lastIndex - 1;
                        } else if (lastElement.length() > 3
                                && lastElement.substring(lastElement.length() - 3).matches("[A-Z]{3}")) {
                            driverCode = lastElement.substring(lastElement.length() - 3);
                            nameData[lastIndex] = lastElement.substring(0, lastElement.length() - 3);
                        } else {
                            driverCode = "N/A";
                            nameEndIndex = lastIndex;
                        }

                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i <= nameEndIndex; i++) {
                            sb.append(nameData[i]).append(" ");
                        }
                        lastName = sb.toString().trim();

                        standing.setLastName(lastName);
                        standing.setDriver_code(driverCode);
                    } else {
                        standing.setLastName("N/A");
                        standing.setDriver_code("N/A");
                    }
                    standing.setNationality(cells.get(2).text().trim());
                    standing.setTeam(cells.get(3).text().trim());
                    standing.setPoints(Integer.parseInt(cells.get(4).text().trim()));
                    scrapedStandings.add(standing);
                }
            }
            logger.info("Successfully scraped {} standings for year {}.", scrapedStandings.size(), year);
        } catch (IOException e) {
            logger.error("Error scraping F1 standings for year {}: {}", year, e.getMessage());
        }
        return scrapedStandings;
    }

    public void updateDriverAndPointsFromScraperForSeason(String year) {
        long startTime = System.nanoTime();
        logger.info("Starting point update for season: {}", year);

        List<ScrapedStanding> scrapedData = scrapeF1Standings(year);
        if (scrapedData.isEmpty()) {
            return;
        }

        Season season = seasonRepository.findBySeasonName(year)
                .orElseThrow(() -> new RuntimeException("Season " + year + " not found"));

        Map<String, Driver> driverCache = new HashMap<>();

        for (var data : scrapedData) {
            try {
                self.processSingleStanding(data, season, driverCache);
            } catch (Exception e) {
                logger.error("Failed to process standing for driver {}: {}", data.getLastName(), e.getMessage());
            }
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logger.info("Finished updating points for season {}. Total duration: {} ms", year, durationMs);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleStanding(ScrapedStanding data, Season season, Map<String, Driver> driverCache) {
        Driver driver = driverCache.computeIfAbsent(data.getLastName(), name -> {
            return driverRepository.findByLastnameIgnoreCase(name)
                    .or(() -> driverRepository.findByFirstnameIgnoreCase(name))
                    .orElseThrow(() -> new RuntimeException("Driver not found: " + name));
        });

        String scrapedTeamName = data.getTeam();
        Team team = teamRepository.findAll()
                .stream()
                .filter(t -> scrapedTeamName.toLowerCase().contains(t.getShortName().toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Team not found for scraped name: " + scrapedTeamName));

        driverTeamSeasonRepository.findByDriverAndSeasonAndTeam(driver, season, team)
                .map(dts -> {
                    if (dts.getPoints() != data.getPoints()) {
                        logger.info("Updating points for {} {} to {}.", driver.getFirstname(), driver.getLastname(),
                                data.getPoints());
                        dts.setPoints(data.getPoints());
                        dts.setUpdated_at(Timestamp.from(Instant.now()));
                        return driverTeamSeasonRepository.save(dts);
                    }
                    return dts;
                })
                .orElseGet(() -> {
                    logger.info("Creating new DTS for {} {} at {}.", driver.getFirstname(), driver.getLastname(),
                            team.getShortName());
                    DriverTeamSeason newDTS = DriverTeamSeason.builder()
                            .driver(driver)
                            .season(season)
                            .team(team)
                            .points(data.getPoints())
                            .updated_at(Timestamp.from(Instant.now()))
                            .created_at(Timestamp.from(Instant.now()))
                            .build();

                    return driverTeamSeasonRepository.save(newDTS);
                });
    }
}