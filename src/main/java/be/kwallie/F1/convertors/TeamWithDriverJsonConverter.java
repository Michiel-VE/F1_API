package be.kwallie.F1.convertors;

import be.kwallie.F1.models.Team;
import be.kwallie.F1.models.response.TeamWithDriverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamWithDriverJsonConverter {
    private final DriverJsonConverter driverJsonConverter;

    public TeamWithDriverResponse teamWithDriverResponseConvert(Team team){

        return TeamWithDriverResponse.builder()
                .drivers(team.getDrivers().stream().map(driverJsonConverter::driverResponseConvert).limit(2).collect(Collectors.toList()))
                .teamId(team.getId())
                .origin(team.getOrigin())
                .name(team.getName())
                .build();
    }
}
