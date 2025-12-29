package be.michielve.f1_api.services;

import be.michielve.f1_api.convertors.DriverConverter;
import be.michielve.f1_api.models.Season;
import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.models.response.DriverCareerHistoryResponse;
import be.michielve.f1_api.models.response.DriverResponse;
import be.michielve.f1_api.models.response.DriverWithSeasonsResponse;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.DriverTeamSeasonRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DriverService {
    // Correct way to initialize the logger
    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;
    private final DriverTeamSeasonRepository driverTeamSeasonRepository;
    private final DriverConverter driverConverter;

    public List<DriverWithSeasonsResponse> getAllDrivers() {
        logger.info("Attempting to retrieve all drivers with their teams and seasons.");
        List<DriverWithSeasonsResponse> drivers = driverRepository.findAllWithTeamsAndSeasons(String.valueOf(Year.now().getValue())).stream()
                .map(driverConverter::driverResponseWithSeasonsConvert)
                .collect(Collectors.toList());
        logger.info("Successfully retrieved {} drivers.", drivers.size());
        return drivers;
    }

    public List<DriverWithSeasonsResponse> getAllDriversForSeason(String season) {
        logger.info("Attempting to retrieve all drivers for season: {}", season);
        List<DriverWithSeasonsResponse> drivers = driverRepository.findAllBySeasonName(season).stream()
                .map(driverConverter::driverResponseWithSeasonsConvert)
                .collect(Collectors.toList());
        logger.info("Successfully retrieved {} drivers for season: {}", drivers.size(), season);
        return drivers;
    }

    public DriverResponse getDriverDetails(int permanentNumber) {
        logger.info("Attempting to find driver with permanent number: {}", permanentNumber);
        return driverRepository
                .findByPermanentNumber(permanentNumber)
                .map(driverConverter::driverResponseConvert)
                .orElseThrow(() -> {
                    logger.error("Driver not found with permanent number: {}", permanentNumber);
                    return new RuntimeException("Driver not found with permanentNumber: " + permanentNumber);
                });
    }

    public List<DriverCareerHistoryResponse> getDriverCareerHistory(String driverName) {
        logger.info("Attempting to find career history for driver: {}", driverName);

        return driverRepository
                .findByLastnameIgnoreCase(driverName)
                .map(driver -> driver.getDriverTeamSeasons().stream()
                        .map(teamSeason -> {
                            String seasonName = Optional.ofNullable(teamSeason.getSeason())
                                    .map(Season::getSeasonName)
                                    .orElse("Unknown Season");

                            String teamName = Optional.ofNullable(teamSeason.getTeam())
                                    .map(Team::getName)
                                    .orElse("Unknown Team");

                            String points = Optional.of(teamSeason.getPoints())
                                    .map(Object::toString)
                                    .orElse("N/A");

                            int position = driverTeamSeasonRepository.findDriverPositionByLastName(driverName);



                            return new DriverCareerHistoryResponse(seasonName, teamName, String.valueOf(position), points);
                        })
                        .collect(Collectors.toList()))
                .orElseThrow(() -> {
                    logger.error("Driver not found with name: {}", driverName);
                    return new RuntimeException("Driver not found with name: " + driverName);
                });
    }

}