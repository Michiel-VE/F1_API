package be.michielve.f1_api.services;

import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.RaceSeason;
import be.michielve.f1_api.models.Season;
import be.michielve.f1_api.repositories.RaceRepository;
import be.michielve.f1_api.repositories.RaceSeasonRepository;
import be.michielve.f1_api.repositories.SeasonRepository;
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
import java.time.Month;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.format.TextStyle;

@Service
@RequiredArgsConstructor
public class ScrapedRaceService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedRaceService.class);

    // CSS selectors centralized for easier maintenance
    private static final String RACE_CARD_SELECTOR = "div.grid.justify-items-stretch a.group";
    private static final String DESCRIPTION_SELECTOR = "p.typography-module_display-xl-bold__Gyl5W";
    private static final String NAME_SELECTOR = "span.typography-module_body-xs-semibold__Fyfwn";
    private static final String DATE_SELECTOR = "span.typography-module_technical-m-bold__JDsxP";
    private static final String DATE_FALLBACK_SELECTOR = "span.typography-module_technical-xs-regular__-W0Gs";

    private final SeasonRepository seasonRepository;
    private final RaceRepository raceRepository;
    private final RaceSeasonRepository raceSeasonRepository;

    public List<Race> scrapeF1Race(String year) {
        long startTime = System.nanoTime();
        List<Race> races = new ArrayList<>();
        logger.info("Starting scrape of F1 races for year {}", year);

        try {
            String url = "https://www.formula1.com/en/racing/" + year;
            logger.debug("Connecting to URL: {}", url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(15000)
                    .get();

            Elements cards = doc.select(RACE_CARD_SELECTOR);
            logger.info("Found {} race cards to process.", cards.size());

            for (Element card : cards) {
                try {
                    String description = safeSelectText(card, DESCRIPTION_SELECTOR);
                    String raceName = safeSelectText(card, NAME_SELECTOR);
                    String dateText = safeSelectText(card, DATE_SELECTOR);
                    if (dateText.isEmpty()) {
                        dateText = safeSelectText(card, DATE_FALLBACK_SELECTOR);
                    }

                    LocalDate[] range = parseDateRange(dateText, year);

                    Race race = new Race();
                    race.setName(raceName);
                    race.setCountry(description);
                    race.setRaceStartDate(range[0]);
                    race.setRaceEndDate(range[1]);
                    races.add(race);
                    logger.trace("Successfully parsed race: {} in {}", raceName, description);
                } catch (Exception e) {
                    logger.warn("Could not parse race card. Error: {}", e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.error("Error loading F1 schedule for year {} via JSoup", year, e);
        }

        long duration = System.nanoTime() - startTime;
        logger.info("Scraping for year {} completed. Total races parsed: {}. Duration: {}ms",
                year, races.size(), TimeUnit.NANOSECONDS.toMillis(duration));

        return races;
    }


    public void updateRaceFromScraperForSeason(String year) {
        long startTime = System.nanoTime();
        logger.info("Starting race update for season {}.", year);

        List<Race> scrapedData = scrapeF1Race(year);
        if (scrapedData.isEmpty()) {
            logger.warn("No scraped data found for season {}. Skipping update.", year);
            return;
        }

        Season season = seasonRepository.findBySeasonName(year)
                .orElseThrow(() -> {
                    logger.error("Season '{}' not found in database.", year);
                    return new RuntimeException("Season " + year + " not found");
                });
        logger.info("Season '{}' found. Processing {} scraped races.", year, scrapedData.size());

        for (Race scrapedRace : scrapedData) {
            Race race = raceRepository.findByNameAndRaceStartDateAndRaceEndDate(
                    scrapedRace.getName(),
                    scrapedRace.getRaceStartDate(),
                    scrapedRace.getRaceEndDate()
            ).orElseGet(() -> {
                logger.info("Race '{}' not found in database. Saving new race.", scrapedRace.getName());
                Race newRace = new Race();
                newRace.setName(scrapedRace.getName());
                newRace.setCountry(scrapedRace.getCountry());
                newRace.setRaceStartDate(scrapedRace.getRaceStartDate());
                newRace.setRaceEndDate(scrapedRace.getRaceEndDate());
                newRace.setUpdated_at(Timestamp.from(Instant.now()));
                newRace.setCreated_at(Timestamp.from(Instant.now()));
                return raceRepository.save(newRace);
            });

            // Check if a RaceSeason entry already exists to prevent duplicates
            boolean associationExists = raceSeasonRepository.existsByRaceAndSeason(race, season);

            if (!associationExists) {
                logger.info("Creating new RaceSeason association for race '{}' and season '{}'.", race.getName(), season.getSeasonName());
                RaceSeason raceSeason = new RaceSeason();
                raceSeason.setRace(race);
                raceSeason.setSeason(season);
                raceSeason.setUpdated_at(Timestamp.from(Instant.now()));
                raceSeason.setCreated_at(Timestamp.from(Instant.now()));
                raceSeasonRepository.save(raceSeason);
            } else {
                logger.debug("Association for race '{}' and season '{}' already exists. Skipping.", race.getName(), season.getSeasonName());
            }
        }

        long duration = System.nanoTime() - startTime;
        logger.info("Race update for season {} completed. Duration: {}ms", year, TimeUnit.NANOSECONDS.toMillis(duration));
    }


    /**
     * Safe method to get text from an element by CSS selector within a parent element.
     * Returns empty string if not found.
     */
    private String safeSelectText(Element parent, String cssSelector) {
        try {
            Element element = parent.selectFirst(cssSelector);
            return element != null ? element.text().trim() : "";
        } catch (Exception e) {
            logger.warn("Element not found or error in selector '{}'.", cssSelector);
            return "";
        }
    }


    /**
     * Parses a date range string like "3-5 Sep" or "3 Sep" into start and end LocalDate objects.
     * Uses regex to extract day(s) and month, constructs dates with the given year.
     */
    private int parseMonth(String monthStr) {
        // Normalize input to uppercase full name for Month enum
        for (Month m : Month.values()) {
            String shortName = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (shortName.equalsIgnoreCase(monthStr)) {
                return m.getValue(); // 1 for Jan, 2 for Feb, etc.
            }
        }
        throw new IllegalArgumentException("Invalid month abbreviation: " + monthStr);
    }

    private LocalDate[] parseDateRange(String range, String year) {
        try {
            Pattern crossMonth = Pattern.compile("(?<startDay>\\d{2}).*?(?<startMonth>[a-zA-Z]{3}).*?(?<endDay>\\d{2}).*?(?<endMonth>[a-zA-Z]{3})");
            Pattern sameMonth = Pattern.compile("(?<startDay>\\d{2}).*?(?<endDay>\\d{2}).*?(?<endMonth>[a-zA-Z]{3})");

            Matcher matcherCrossMonth = crossMonth.matcher(range);
            Matcher matcherSameMonth = sameMonth.matcher(range);

            int startDay;
            int endDay;
            int startMonth;
            int endMonth;

            if (matcherCrossMonth.find()) {
                startDay = Integer.parseInt(matcherCrossMonth.group("startDay"));
                endDay = Integer.parseInt(matcherCrossMonth.group("endDay"));
                startMonth = parseMonth(matcherCrossMonth.group("startMonth"));
                endMonth = parseMonth(matcherCrossMonth.group("endMonth"));
            } else if (matcherSameMonth.find()) {
                startDay = Integer.parseInt(matcherSameMonth.group("startDay"));
                endDay = Integer.parseInt(matcherSameMonth.group("endDay"));
                startMonth = parseMonth(matcherSameMonth.group("endMonth"));
                endMonth = startMonth;
            } else {
                throw new RuntimeException("Date range format not recognized: " + range);
            }

            int yearInt = Integer.parseInt(year);
            LocalDate startDate = LocalDate.of(yearInt, startMonth, startDay);
            LocalDate endDate = LocalDate.of(yearInt, endMonth, endDay);

            return new LocalDate[]{startDate, endDate};
        } catch (Exception e) {
            logger.error("Failed to parse date range: {}", range, e);
            throw new RuntimeException("Invalid date range format: " + range, e);
        }
    }
}