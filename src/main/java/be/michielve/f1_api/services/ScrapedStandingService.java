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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Time;
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

    private String TABLE_BODY_CONTENT_SELECTOR = "table tbody";

    public List<ScrapedStanding> scrapeF1Standings(String year) {
        long startTime = System.nanoTime();
        List<ScrapedStanding> scrapedStandings = new ArrayList<>();
        logger.info("Starting to scrape F1 standings for year {}", year);

        try {
            String url = "https://www.formula1.com/en/results/" + year + "/drivers";
            logger.debug("Connecting to URL: {}", url);
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
            logger.info("Found {} rows in standings table. Parsing data...", rows.size());

            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() >= 5) {
                    ScrapedStanding standing = new ScrapedStanding();
                    standing.setPosition(Integer.parseInt(cells.get(0).text().trim()));
                    String[] nameData = cells.get(1).text().trim().split(" ");
                    standing.setFirstName(nameData[0]);
                    // Logic to handle potential IndexOutOfBoundsException for last name
                    if (nameData.length > 1) {
                        int lastIndex = nameData.length - 1;
                        String lastElement = nameData[lastIndex];
                        String driverCode;
                        String lastName;
                        int nameEndIndex = lastIndex; // Index of the last word that is part of the name

                        // Case 1: Driver code is a standalone element (e.g., ["Nick", "de", "Vries",
                        // "DEV"])
                        if (lastElement.length() == 3 && lastElement.matches("[A-Z]{3}") && lastIndex > 0) {
                            driverCode = lastElement;
                            nameEndIndex = lastIndex - 1; // The name ends before the code element

                            // Case 2: Driver code is appended to the last name (e.g., ["Nick", "de",
                            // "VRIESDEV"])
                        } else if (lastElement.length() > 3
                                && lastElement.substring(lastElement.length() - 3).matches("[A-Z]{3}")) {
                            driverCode = lastElement.substring(lastElement.length() - 3);
                            // We temporarily remove the code from the last element, but still need to
                            // include it in the reconstruction loop
                            nameData[lastIndex] = lastElement.substring(0, lastElement.length() - 3);

                            // Case 3: No driver code found, the whole rest of the array is the last name
                            // (e.g., ["Nick", "de", "Vries"])
                        } else {
                            driverCode = "N/A";
                            nameEndIndex = lastIndex; // The last word is part of the name
                        }

                        // --- Name Reconstruction Loop (THE FIX) ---
                        // Start at index 1 (skipping the assumed First Name: "Nick")
                        // End at nameEndIndex (to stop before a separate driver code, or at the end of
                        // the list)
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
                    logger.trace("Parsed standing: {}", standing);
                }
            }
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            long durationSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            logger.info("Successfully scraped {} standings for year {}. Scrape duration: {} ms ({} seconds)",
                    scrapedStandings.size(), year, durationMs, durationSeconds);
        } catch (IOException e) {
            logger.error("Error scraping F1 standings for year {}. Reason: {}", year, e.getMessage(), e);
        }
        return scrapedStandings;
    }

    public void updateDriverAndPointsFromScraperForSeason(String year) {
        long startTime = System.nanoTime();
        logger.info("Starting point update for season: {}", year);

        List<ScrapedStanding> scrapedData = scrapeF1Standings(year);
        if (scrapedData.isEmpty()) {
            logger.warn("No data scraped for year {}. Skipping point update.", year);
            return;
        }

        try {
            Season season = seasonRepository.findBySeasonName(year)
                    .orElseThrow(() -> new RuntimeException("Season " + year + " not found"));
            logger.info("Found season '{}' in repository. Proceeding with updates.", year);

            Map<String, Driver> driverCache = new HashMap<>();

            for (var data : scrapedData) {
                logger.debug("Processing scraped data for driver: {}", data.getLastName());

                var driver = driverCache.computeIfAbsent(data.getLastName(), name -> {
                    var foundDriver = driverRepository.findByLastnameIgnoreCase(name)
                            .or(() -> driverRepository.findByFirstnameIgnoreCase(name))
                            .orElse(null);

                    if (foundDriver == null) {
                        logger.error("Driver not found in repository for name: {}", name);
                    }

                    return foundDriver;
                });

                assert driver != null;
                Optional<Team> teamOptional = teamRepository.findByShortName(data.getTeam());

                teamOptional.ifPresentOrElse(team -> {
                    driverTeamSeasonRepository.findByDriverAndSeasonAndTeam(driver, season, team)
                            .map(dts -> {
                                if (dts.getPoints() != data.getPoints()) {
                                    logger.info("Updating points for driver {} {} in team {} from {} to {}.",
                                            driver.getFirstname(), driver.getLastname(), team.getName(),
                                            dts.getPoints(), data.getPoints());
                                    dts.setPoints(data.getPoints());
                                    dts.setUpdated_at(Timestamp.from(Instant.now()));
                                    return driverTeamSeasonRepository.save(dts);
                                } else {
                                    logger.debug("Points for driver {} {} in team {} already up-to-date.",
                                            driver.getFirstname(), driver.getLastname(), team.getName());
                                    return dts;
                                }
                            })
                            .orElseGet(() -> {
                                DriverTeamSeason newDTS = DriverTeamSeason.builder()
                                        .driver(driver)
                                        .season(season)
                                        .team(team)
                                        .points(data.getPoints())
                                        .updated_at(Timestamp.from(Instant.now()))
                                        .created_at(Timestamp.from(Instant.now()))
                                        .build();
                                logger.info("Creating new DriverTeamSeason for driver {} {} in team {} with points: {}",
                                        driver.getFirstname(), driver.getLastname(), team.getName(), data.getPoints());
                                return driverTeamSeasonRepository.save(newDTS);
                            });
                }, () -> {
                    logger.error("Team not found for driver {} {}", driver.getFirstname(), driver.getLastname());
                });

            }

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            long durationSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            logger.info("Finished updating points for season {}. Total duration: {} ms ({} seconds)", year, durationMs,
                    durationSeconds);
        } catch (RuntimeException e) {
            logger.error("Failed to update driver points for season {}. Error: {}", year, e.getMessage(), e);
        }
    }
}