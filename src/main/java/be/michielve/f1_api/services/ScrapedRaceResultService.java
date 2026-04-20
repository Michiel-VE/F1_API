package be.michielve.f1_api.services;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverRaceResult;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.Season;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.DriverRaceResultRepository;
import be.michielve.f1_api.repositories.RaceRepository;
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
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrapedRaceResultService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedRaceResultService.class);

    @Value("${f1.api.base-url:https://www.formula1.com}")
    private String baseUrl;

    private final DriverRepository driverRepository;
    private final RaceRepository raceRepository;
    private final SeasonRepository seasonRepository;
    private final DriverRaceResultRepository driverRaceResultRepository;

    @Autowired
    @Lazy
    private ScrapedRaceResultService self;

    public void updatePendingRaceResults(String year) {
        logger.info("Syncing completed race results for {} up to current date.", year);

        // The repository now handles filtering out Testing events via SQL
        Optional<Race> targetRaceOpt = raceRepository.findFirstFinishedRaceMissingResults(Timestamp.from(Instant.now()), year);

        if (targetRaceOpt.isEmpty()) {
            logger.info("All finished races for {} already have results recorded.", year);
            return;
        }

        Race targetRace = targetRaceOpt.get();
        Season season = seasonRepository.findBySeasonName(year)
                .orElseThrow(() -> new RuntimeException("Season not found: " + year));

        logger.info("Targeting race: {}", targetRace.getName());

        List<String> resultUrls = discoverRaceResultUrls(year);
        String matchSlug = getF1WebsiteSlug(targetRace);
        
        String targetUrl = resultUrls.stream()
                .filter(url -> url.contains("/" + matchSlug + "/"))
                .findFirst()
                .orElse(null);

        if (targetUrl != null) {
            self.scrapeAndPersistRaceResult(targetUrl, season, targetRace);
        } else {
            logger.warn("F1.com result page not found for slug: {}. Skipping for now.", matchSlug);
        }
    }

    private String getF1WebsiteSlug(Race race) {
        String country = race.getCountry().toLowerCase();
        String name = race.getName().toLowerCase();

        if (name.contains("barcelona")) return "barcelona-catalunya";
        if (country.equals("saudi arabia")) return "saudi-arabia";
        if (country.equals("united states") || country.equals("usa")) return "usa";
        if (country.equals("great britain")) return "great-britain";
        if (country.equals("uae") || name.contains("abu dhabi")) return "abu-dhabi";

        return country.replace(" ", "-");
    }

    private List<String> discoverRaceResultUrls(String year) {
        List<String> urls = new ArrayList<>();
        String indexUrl = baseUrl + "/en/results/" + year + "/races";
        try {
            Document doc = Jsoup.connect(indexUrl).timeout(15000).get();
            doc.select("a[href*=/race-result]").forEach(a -> {
                String href = a.attr("href");
                urls.add(href.startsWith("http") ? href : baseUrl + href);
            });
        } catch (IOException e) {
            logger.error("Failed to fetch results index: {}", e.getMessage());
        }
        return urls;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scrapeAndPersistRaceResult(String resultUrl, Season season, Race race) {
        logger.info("Scraping results from: {}", resultUrl);
        try {
            Document doc = Jsoup.connect(resultUrl).timeout(15000).get();
            Elements rows = doc.select("table tbody tr");
            for (Element row : rows) {
                parseAndSaveRow(row, race, season);
            }
            logger.info("Saved results for {}", race.getName());
        } catch (IOException e) {
            logger.error("Could not scrape results: {}", resultUrl);
        }
    }

    private void parseAndSaveRow(Element row, Race race, Season season) {
        Elements cells = row.select("td");
        if (cells.size() < 7) return;

        String driverCode = cells.get(2).select("span.uppercase").last() != null
                ? cells.get(2).select("span.uppercase").last().text().trim()
                : cells.get(2).text().replaceAll(".*?([A-Z]{3})$", "$1").trim();

        Driver driver = driverRepository.findByDriverCode(driverCode).orElse(null);
        if (driver == null || driverRaceResultRepository.existsByDriverAndRaceAndSeason(driver, race, season)) {
            return;
        }

        DriverRaceResult res = new DriverRaceResult();
        res.setDriver(driver);
        res.setRace(race);
        res.setSeason(season);
        
        try { res.setPoints(new BigDecimal(cells.get(6).text().trim())); } catch (Exception e) { res.setPoints(BigDecimal.ZERO); }
        try { res.setLapsCompleted(Integer.parseInt(cells.get(4).text().trim())); } catch (Exception e) {}
        
        res.setStatus(deriveStatus(cells.get(0).text().trim(), cells.get(5).text().trim()));
        res.setCreated_at(Timestamp.from(Instant.now()));
        res.setUpdated_at(Timestamp.from(Instant.now()));

        driverRaceResultRepository.save(res);
    }

    private String deriveStatus(String pos, String time) {
        if (!pos.equalsIgnoreCase("NC")) return "Finished";
        if (time.equalsIgnoreCase("DSQ")) return "DSQ";
        if (time.equalsIgnoreCase("DNS")) return "DNS";
        return "DNF";
    }
}