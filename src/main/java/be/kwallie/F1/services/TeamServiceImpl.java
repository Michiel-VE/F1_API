package be.kwallie.F1.services;

import be.kwallie.F1.convertors.TeamJsonConverter;
import be.kwallie.F1.models.Driver;
import be.kwallie.F1.models.Team;
import be.kwallie.F1.models.response.DriverResponse;
import be.kwallie.F1.models.response.TeamResponse;
import be.kwallie.F1.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamJsonConverter teamJsonConverter;

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(teamJsonConverter::teamResponseConvert)
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponse getTeam(Long id) {
        Team team = teamRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));

        return teamJsonConverter.teamResponseConvert(team);
    }
}
