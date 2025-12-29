package be.michielve.f1_api.services;

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
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ScrapedTeamService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedTeamService.class);

    public static final String TEAM_CARD_SELECTOR = "a[data-f1rd-a7s-click=\"team_card_click\"]";
    public static final String TEAM_CARD_SHORTNAME = "div.relative.z-40.w-full.flex.justify-center.gap-px-32 > h1";
    public static final String TEAM_CARD_NAME = "dl.DataGrid-module_dataGrid__Zk5Y8.DataGrid-module_columns__rcgN8 > div > dd";
    public static final String TEAM_CARD_BASE_COUNTRY = "dl.DataGrid-module_dataGrid__Zk5Y8.DataGrid-module_columns__rcgN8 > div:nth-child(2) > dd";

    private final TeamRepository teamRepository;

    /**
     * Scrapes F1 team data from the official Formula 1 website.
     * It first gets a list of teams from the main team page, then follows each
     * team's link to scrape their individual details.
     *
     * @return A list of Team objects with scraped data.
     */
    public List<Team> scrapeF1Teams() {
        long startTime = System.nanoTime();
        List<Team> teams = new ArrayList<>();
        logger.info("Starting scrape of F1 teams.");

        try {
            String url = "https://www.formula1.com/en/teams";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                    .timeout(15000).get();

            Elements cards = doc.select(TEAM_CARD_SELECTOR);
            logger.info("Found {} team cards to process.", cards.size());

            for (Element card : cards) {
                try {
                    String teamPageUrl = "https://www.formula1.com" + card.attr("href");
                    Document teamDoc = Jsoup.connect(teamPageUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                            .timeout(15000).get();

                    String shortName = Helpers.safeSelectText(teamDoc.selectFirst(TEAM_CARD_SHORTNAME));
                    String name = Helpers.safeSelectText(teamDoc.selectFirst(TEAM_CARD_NAME));
                    String town_country = Helpers.safeSelectText(teamDoc.selectFirst(TEAM_CARD_BASE_COUNTRY));
                    String country = town_country.split(",")[1].trim();
                    String town = town_country.split(",")[0].trim();

                    Team team = Team.builder()
                            .name(name)
                            .shortName(shortName)
                            .country(country)
                            .base(town)
                            .updated_at(Timestamp.from(Instant.now()))
                            .created_at(Timestamp.from(Instant.now()))
                            .build();

                    teams.add(team);

                } catch (Exception e) {
                    logger.warn("Could not parse driver card. Error: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error loading F1 drivers page via JSoup", e);
        }

        long duration = System.nanoTime() - startTime;
        logger.info("Scraping for drivers completed. Total drivers parsed: {}. Duration: {}ms",
                teams.size(), TimeUnit.NANOSECONDS.toSeconds(duration));

        return teams;
    }

    public void updateTeamsFromScraper() {
        List<Team> scrapedTeams = scrapeF1Teams();

        for (Team scrapedTeam : scrapedTeams) {
            Optional<Team> existingTeam = teamRepository.findByName(scrapedTeam.getName());

            if (existingTeam.isEmpty()) {
                teamRepository.save(scrapedTeam);
            } else {
                System.out.println("Team already exists: " + scrapedTeam.getName());
            }

        }
    }
}
