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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String RACE_CARD_SELECTOR = "div.grid.justify-items-stretch a.group";
    private static final String DESCRIPTION_SELECTOR = "p.typography-module_display-xl-bold__Gyl5W";
    private static final String NAME_SELECTOR = "span.typography-module_body-xs-semibold__Fyfwn";
    private static final String DATE_SELECTOR = "span.typography-module_technical-m-bold__JDsxP";
    private static final String DATE_FALLBACK_SELECTOR = "span.typography-module_technical-xs-regular__-W0Gs";

    private final SeasonRepository seasonRepository;
    private final RaceRepository raceRepository;
    private final RaceSeasonRepository raceSeasonRepository;

    @Value("${f1.api.base-url:https://www.formula1.com}")
    private String baseUrl;

    @Autowired
    @Lazy
    private ScrapedRaceService self;

    public List<Race> scrapeF1Race(String year) {
        long startTime = System.nanoTime();
        List<Race> races = new ArrayList<>();
        logger.info("Starting scrape of F1 races for year {}", year);

        try {
            String url = baseUrl + "/en/racing/" + year;
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(15000)
                    .get();

            Elements cards = doc.select(RACE_CARD_SELECTOR);
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
                } catch (Exception e) {
                    logger.warn("Could not parse race card for year {}. Error: {}", year, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error loading F1 schedule for year {}: {}", year, e.getMessage());
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

        // Call via 'self' proxy to ensure individual transaction
        Season season = self.getOrCreateSeason(year);

        logger.info("Processing {} scraped races for season '{}'.", scrapedData.size(), year);

        for (Race scrapedRace : scrapedData) {
            try {
                // Call via 'self' proxy to ensure individual transaction for each race
                self.processSingleRace(scrapedRace, season);
            } catch (Exception e) {
                logger.error("Failed to process race '{}' for season {}: {}", scrapedRace.getName(), year, e.getMessage());
            }
        }

        long duration = System.nanoTime() - startTime;
        logger.info("Race update for season {} completed. Duration: {}ms", year, TimeUnit.NANOSECONDS.toMillis(duration));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Season getOrCreateSeason(String year) {
        return seasonRepository.findBySeasonName(year)
                .orElseGet(() -> {
                    logger.info("Season '{}' not found. Creating new season record.", year);
                    Season newSeason = new Season();
                    newSeason.setSeasonName(year);
                    newSeason.setCreated_at(Timestamp.from(Instant.now()));
                    newSeason.setUpdated_at(Timestamp.from(Instant.now()));
                    return seasonRepository.save(newSeason);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleRace(Race scrapedRace, Season season) {
        Race race = raceRepository.findByNameAndRaceStartDateAndRaceEndDate(
                scrapedRace.getName(),
                scrapedRace.getRaceStartDate(),
                scrapedRace.getRaceEndDate()
        ).orElseGet(() -> {
            logger.info("Race '{}' not found. Saving new race record.", scrapedRace.getName());
            Race newRace = new Race();
            newRace.setName(scrapedRace.getName());
            newRace.setCountry(scrapedRace.getCountry());
            newRace.setRaceStartDate(scrapedRace.getRaceStartDate());
            newRace.setRaceEndDate(scrapedRace.getRaceEndDate());
            newRace.setUpdated_at(Timestamp.from(Instant.now()));
            newRace.setCreated_at(Timestamp.from(Instant.now()));
            return raceRepository.save(newRace);
        });

        if (!raceSeasonRepository.existsByRaceAndSeason(race, season)) {
            logger.info("Linking race '{}' to season '{}'.", race.getName(), season.getSeasonName());
            RaceSeason raceSeason = new RaceSeason();
            raceSeason.setRace(race);
            raceSeason.setSeason(season);
            raceSeason.setUpdated_at(Timestamp.from(Instant.now()));
            raceSeason.setCreated_at(Timestamp.from(Instant.now()));
            raceSeasonRepository.save(raceSeason);
        }
    }

    private String safeSelectText(Element parent, String cssSelector) {
        try {
            Element element = parent.selectFirst(cssSelector);
            return element != null ? element.text().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private int parseMonth(String monthStr) {
        for (Month m : Month.values()) {
            String shortName = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (shortName.equalsIgnoreCase(monthStr)) return m.getValue();
        }
        throw new IllegalArgumentException("Invalid month: " + monthStr);
    }

    private LocalDate[] parseDateRange(String range, String year) {
        try {
            Pattern crossMonth = Pattern.compile("(?<startDay>\\d{2}).*?(?<startMonth>[a-zA-Z]{3}).*?(?<endDay>\\d{2}).*?(?<endMonth>[a-zA-Z]{3})");
            Pattern sameMonth = Pattern.compile("(?<startDay>\\d{2}).*?(?<endDay>\\d{2}).*?(?<endMonth>[a-zA-Z]{3})");

            Matcher matcherCrossMonth = crossMonth.matcher(range);
            Matcher matcherSameMonth = sameMonth.matcher(range);

            int startDay, endDay, startMonth, endMonth;

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
                throw new IllegalArgumentException("Unknown date format: " + range);
            }

            int yearInt = Integer.parseInt(year);
            return new LocalDate[]{LocalDate.of(yearInt, startMonth, startDay), LocalDate.of(yearInt, endMonth, endDay)};
        } catch (Exception e) {
            throw new RuntimeException("Invalid date range format: " + range, e);
        }
    }
}