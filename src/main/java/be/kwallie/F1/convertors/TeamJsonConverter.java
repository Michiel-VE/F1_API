package be.kwallie.F1.convertors;

import be.kwallie.F1.models.Team;
import be.kwallie.F1.models.response.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TeamJsonConverter {
    public TeamResponse teamResponseConvert(Team team){
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .origin(team.getOrigin())
                .build();
    }
}
