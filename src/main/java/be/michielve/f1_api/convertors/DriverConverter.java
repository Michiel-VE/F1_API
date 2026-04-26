package be.michielve.f1_api.convertors;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverRaceResult;
import be.michielve.f1_api.models.DriverTeamSeason;
import be.michielve.f1_api.models.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DriverConverter {

    public DriverWithSeasonsResponse driverResponseWithSeasonsConvert(Driver driver) {
        return DriverWithSeasonsResponse.builder()
                .driverId(driver.getId())
                .firstname(driver.getFirstname())
                .lastname(driver.getLastname())
                .permanentNumber(driver.getPermanentNumber())
                .birthday(driver.getBirthday())
                .country(driver.getCountry())
                .countryCode(driver.getCountryCode())
                .teamSeasons(
                        driver.getDriverTeamSeasons().stream()
                                .map(this::convertTeamSeason)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public DriverResponse driverResponseConvert(Driver driver){
        return DriverResponse.builder()
                .driverId(driver.getId())
                .firstname(driver.getFirstname())
                .lastname(driver.getLastname())
                .teamName(driver.getTeam())
                .permanentNumber(driver.getPermanentNumber())
                .birthday(driver.getBirthday())
                .country(driver.getCountry())
                .countryCode(driver.getCountryCode())
                .created_at(driver.getCreated_at())
                .build();
    }

    public DriverRaceResultsResponse toDriverRaceResultsResponse(Driver driver, List<DriverRaceResult> results, String season) {
        return DriverRaceResultsResponse.builder()
                .firstname(driver.getFirstname())
                .lastname(driver.getLastname())
                .season(season)
                .results(results.stream()
                        .map(this::toRaceResultDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private RaceResultDTO toRaceResultDTO(DriverRaceResult result) {
        return RaceResultDTO.builder()
                .raceName(result.getRace().getName())
                .points(result.getPoints())
                .status(result.getStatus())
                .lapsCompleted(result.getLapsCompleted())
                .build();
    }

    private TeamSeasonResponse convertTeamSeason(DriverTeamSeason dts) {
        return TeamSeasonResponse.builder()
                .teamName(dts.getTeam().getName())
                .shortName(dts.getTeam().getShortName())
                .seasonName(dts.getSeason().getSeasonName())
                .points(dts.getPoints())
                .created_at(dts.getCreated_at())
                .build();
    }
}