package be.kwallie.F1.services;

import be.kwallie.F1.convertors.TeamJsonConverter;
import be.kwallie.F1.convertors.TeamWithDriverJsonConverter;
import be.kwallie.F1.models.Team;
import be.kwallie.F1.models.response.TeamResponse;
import be.kwallie.F1.models.response.TeamWithDriverResponse;
import be.kwallie.F1.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@AllArgsConstructor
@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamJsonConverter teamJsonConverter;
    private final TeamWithDriverJsonConverter teamWithDriverJsonConverter;

    @Override
    public List<TeamWithDriverResponse> getAllTeamsWithDrivers() {
        return null;
//                teamRepository.getTeamsWithDrivers().stream()
//                .map(teamWithDriverJsonConverter::teamWithDriverResponseConvert)
//                .collect(Collectors.toList());
    }

    @Override
    public TeamResponse getTeam(Long id) {
        Team team = teamRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));

        return teamJsonConverter.teamResponseConvert(team);
    }
}
