package be.kwallie.F1.convertors;

import be.kwallie.F1.models.TeamWithDriver;
import be.kwallie.F1.models.response.TeamWithDriverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TeamWithDriverJsonConverter {
    public TeamWithDriverResponse teamWithDriverResponseConvert(TeamWithDriverResponse teamWithDriver){
        return TeamWithDriverResponse.builder()
                .driverId(teamWithDriver.getDriverId())
                .firstName(teamWithDriver.getFirstName())
                .lastName(teamWithDriver.getLastName())
                .permanentNumber(teamWithDriver.getPermanentNumber())
                .code(teamWithDriver.getCode())
                .birthday(teamWithDriver.getBirthday())
                .picture(teamWithDriver.getPicture())
                .country(teamWithDriver.getCountry())
                .countryCode(teamWithDriver.getCountryCode())
                .wins(teamWithDriver.getWins())
                .points(teamWithDriver.getPoints())
                .penaltyPoints(teamWithDriver.getPenaltyPoints())
                .teamId(teamWithDriver.getTeamId())
                .name(teamWithDriver.getName())
                .origin(teamWithDriver.getOrigin())
                .build();
    }
}
