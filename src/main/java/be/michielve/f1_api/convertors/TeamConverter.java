package be.michielve.f1_api.convertors;

import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.models.response.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class TeamConverter {

    public TeamResponse teamResponseConverter(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .shortName(team.getShortName())
                .country(team.getCountry())
                .base(team.getBase())
                .created_at(team.getCreated_at())
                .build();
    }
}
