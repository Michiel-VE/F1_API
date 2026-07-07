package be.michielve.f1_api.services;

import be.michielve.f1_api.convertors.DriverConverter;
import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.response.DriverCareerHistoryResponse;
import be.michielve.f1_api.models.response.DriverResponse;
import be.michielve.f1_api.models.response.DriverWithSeasonsResponse;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.DriverTeamSeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DriverService {
        private final DriverRepository driverRepository;
        private final DriverTeamSeasonRepository driverTeamSeasonRepository;
        private final DriverConverter driverConverter;

        @Cacheable(cacheResolver = "driverCacheResolver", key = "'season:' + #season")
        public List<DriverWithSeasonsResponse> getAllDriversForSeason(String season) {
                return driverRepository.findAllBySeasonName(season).stream()
                                .map(driverConverter::driverResponseWithSeasonsConvert)
                                .collect(Collectors.toList());
        }

        public List<DriverResponse> getDriverDetails(int permanentNumber) {
                List<Driver> drivers = driverRepository.findByPermanentNumber(permanentNumber);
                if (drivers.isEmpty())
                        throw new RuntimeException("No drivers found with number: " + permanentNumber);
                return drivers.stream().map(driverConverter::driverResponseConvert).collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<DriverCareerHistoryResponse> getDriverCareerHistory(String driverName) {
                return driverRepository.findByLastnameIgnoreCase(driverName)
                                .map(driver -> driver.getDriverTeamSeasons().stream()
                                                .sorted(Comparator.comparing(
                                                                (teamSeason) -> teamSeason.getSeason() != null
                                                                                ? teamSeason.getSeason().getSeasonName()
                                                                                : "",
                                                                Comparator.reverseOrder()))
                                                .map(teamSeason -> {
                                                        String seasonName = Optional.ofNullable(teamSeason.getSeason())
                                                                        .map(season -> season.getSeasonName())
                                                                        .orElse("Unknown");
                                                        String teamName = Optional.ofNullable(teamSeason.getTeam())
                                                                        .map(team -> team.getName())
                                                                        .orElse("Unknown");
                                                        String points = Optional.ofNullable(teamSeason.getPoints())
                                                                        .map(p -> p.toString())
                                                                        .orElse("N/A");
                                                        Integer pos = driverTeamSeasonRepository
                                                                        .findDriverPositionByDtsId(teamSeason.getId());
                                                        return new DriverCareerHistoryResponse(seasonName, teamName,
                                                                        pos != null ? String.valueOf(pos) : "N/A",
                                                                        points);
                                                }).collect(Collectors.toList()))
                                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverName));
        }
}