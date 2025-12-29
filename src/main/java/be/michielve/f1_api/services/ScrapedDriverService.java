package be.michielve.f1_api.services;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.Season;
import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.SeasonRepository;
import be.michielve.f1_api.repositories.TeamRepository;
import be.michielve.f1_api.utils.Helpers;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ScrapedDriverService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedDriverService.class);

    // CSS selectors for the main drivers list page
    private static final String DRIVER_CARD_SELECTOR = "a[data-f1rd-a7s-click=\"driver_card_click\"]";

    // Selectors for the individual driver profile page, updated for the new HTML
    private static final String DRIVER_FIRSTNAME_SELECTOR = "h1 > span.-mb-px-8 > span";
    private static final String DRIVER_LASTNAME_SELECTOR = "h1 > span.typography-module_display-3-xl-black__CWhFe";
    private static final String DRIVER_BIRTHDAY_SELECTOR = "dt:contains(Date of Birth) + dd";

    // Updated selectors based on the provided HTML snippet
    private static final String DRIVER_INFO_GROUP_SELECTOR = ".grow.flex.flex-col.justify-end.md\\:justify-around.gap-px-16.text-static-static-1 > div:nth-child(1) > div.flex.gap-px-12.items-center";
    private static final String DRIVER_COUNTRY_SELECTOR = "p:nth-child(2)";
    private static final String DRIVER_TEAM_SELECTOR = "p:nth-child(3)";
    private static final String DRIVER_NUMBER_SELECTOR = "p:nth-child(5)";


    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final SeasonRepository seasonRepository;

    /**
     * Scrapes F1 driver data from the official Formula 1 website.
     * It first gets a list of drivers from the main drivers page, then follows each
     * driver's link to scrape their individual details.
     *
     * @return A list of Driver objects with scraped data.
     */
    public List<Driver> scrapeF1Drivers() {
        long startTime = System.nanoTime();
        List<Driver> drivers = new ArrayList<>();
        logger.info("Starting scrape of F1 drivers.");

        try {
            String url = "https://www.formula1.com/en/drivers";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                    .timeout(15000).get();

            Elements cards = doc.select(DRIVER_CARD_SELECTOR);
            logger.info("Found {} driver cards to process.", cards.size());

            for (Element card : cards) {
                try {
                    String driverPageUrl = "https://www.formula1.com" + card.attr("href");
                    Document driverDoc = Jsoup.connect(driverPageUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                            .timeout(15000).get();

                    String firstName = Helpers.safeSelectText(driverDoc.selectFirst(DRIVER_FIRSTNAME_SELECTOR));
                    String lastName = Helpers.safeSelectText(driverDoc.selectFirst(DRIVER_LASTNAME_SELECTOR));

                    Element infoGroup = driverDoc.selectFirst(DRIVER_INFO_GROUP_SELECTOR);

                    String country = "";
                    String teamName = "";
                    int permanentNumber = 0;

                    if (infoGroup != null) {
                        Element countryElement = infoGroup.selectFirst(DRIVER_COUNTRY_SELECTOR);
                        country = Helpers.safeSelectText(countryElement);

                        teamName = Helpers.safeSelectText(infoGroup.selectFirst(DRIVER_TEAM_SELECTOR));
                        String numberText = Helpers.safeSelectText(infoGroup.selectFirst(DRIVER_NUMBER_SELECTOR));

                        if (!numberText.isEmpty()) {
                            try {
                                permanentNumber = Integer.parseInt(numberText);
                            } catch (NumberFormatException e) {
                                logger.warn("Could not parse permanent number from '{}'", numberText);
                            }
                        }
                    }


                    String countryCode = Helpers.getCountryCode(country);
                    LocalDate birthday = parseBirthday(Helpers.safeSelectText(driverDoc.selectFirst(DRIVER_BIRTHDAY_SELECTOR)));

                    String driverCode = getDriverCode(lastName);

                    Driver driver = Driver.builder()
                            .firstname(firstName)
                            .lastname(lastName)
                            .permanentNumber(permanentNumber)
                            .driverCode(driverCode)
                            .birthday(birthday)
                            .country(country)
                            .countryCode(countryCode)
                            .team(teamName)
                            .created_at(Timestamp.from(Instant.now()))
                            .updated_at(Timestamp.from(Instant.now()))
                            .build();

                    drivers.add(driver);
                    logger.trace("Successfully parsed driver: {}", driver.getFirstname() + " " + driver.getLastname());

                } catch (Exception e) {
                    logger.warn("Could not parse driver card. Error: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error loading F1 drivers page via JSoup", e);
        }

        long duration = System.nanoTime() - startTime;
        logger.info("Scraping for drivers completed. Total drivers parsed: {}. Duration: {}ms",
                drivers.size(), TimeUnit.NANOSECONDS.toSeconds(duration));

        return drivers;
    }

    public void updateDriversFromScraper() {
        List<Driver> scrapedDrivers = scrapeF1Drivers();

        for (Driver scrapedDriver : scrapedDrivers) {
            Optional<Driver> existingDriver = driverRepository.findByFirstnameAndLastnameIgnoreCase(scrapedDriver.getFirstname(), scrapedDriver.getLastname());

            if (existingDriver.isEmpty()) {
                driverRepository.save(scrapedDriver);
                logger.info("Saved new driver: {}", scrapedDriver.getLastname());
            } else {
                Driver existing = existingDriver.get();
                boolean isDifferent = false;

                if (!existing.getFirstname().equals(scrapedDriver.getFirstname())) {
                    existing.setFirstname(scrapedDriver.getFirstname());
                    isDifferent = true;
                }

                if (!existing.getLastname().equals(scrapedDriver.getLastname())) {
                    existing.setLastname(scrapedDriver.getLastname());
                    isDifferent = true;
                }

                if (!existing.getBirthday().equals(scrapedDriver.getBirthday())) {
                    existing.setBirthday(scrapedDriver.getBirthday());
                    isDifferent = true;
                }

                if (!existing.getCountry().equals(scrapedDriver.getCountry())) {
                    existing.setCountry(scrapedDriver.getCountry());
                    isDifferent = true;
                }

                if (!existing.getTeam().equals(scrapedDriver.getTeam())) {
                    existing.setTeam(scrapedDriver.getTeam());
                    isDifferent = true;
                }

                if (!existing.getUpdated_at().equals(scrapedDriver.getUpdated_at())) {
                    existing.setUpdated_at(scrapedDriver.getUpdated_at());
                    isDifferent = true;
                }

                if (isDifferent) {
                    driverRepository.save(existing);
                    logger.info("Updated driver: {} {}", existing.getFirstname(), existing.getLastname());
                } else {
                    logger.info("Driver data is the same, no update: {} {}", scrapedDriver.getLastname(), scrapedDriver.getFirstname());
                }
            }
        }
    }

    /**
     * Parses a date string into a LocalDate object using a specific format.
     *
     * @param dateStr The date string to parse.
     * @return A LocalDate object or null if parsing fails.
     */
    private LocalDate parseBirthday(String dateStr) {
        try {
            // The format has changed from "d MMMM yyyy" to "dd/MM/yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            logger.error("Failed to parse birthday: {}", dateStr, e);
            return null;
        }
    }

    private String getDriverCode(String lastname) {
        if (lastname == null || lastname.length() < 3) {
            return "";
        }
        return lastname.substring(0, 3).toUpperCase();
    }
}
