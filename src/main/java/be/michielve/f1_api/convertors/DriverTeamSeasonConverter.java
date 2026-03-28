package be.michielve.f1_api.convertors;


import org.springframework.stereotype.Component;

import be.michielve.f1_api.interfaces.TeamWithPoints;
import be.michielve.f1_api.models.response.TeamWithPointsResponse;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class DriverTeamSeasonConverter {
    public TeamWithPointsResponse teamResponseConverter(TeamWithPoints team) {
        return TeamWithPointsResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .shortName(team.getShortName())
                .country(team.getCountry())
                .base(team.getBase())
                .totalPoints(team.getTotalPoints())
                .build();
    }
    
}
