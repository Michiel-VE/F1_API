package be.michielve.f1_api.services;

import be.michielve.f1_api.convertors.TeamConverter;
import be.michielve.f1_api.models.Team;
import be.michielve.f1_api.models.response.TeamResponse;
import be.michielve.f1_api.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final TeamConverter teamConverter;

    public List<TeamResponse> getAllTeamsForSeason(String season) {
        logger.info("Fetching all teams for season: {}", season);
        List<TeamResponse> teams = teamRepository.findAllTeamsBySeasonName(season).stream()
                .map(teamConverter::teamResponseConverter)
                .collect(Collectors.toList());
        logger.info("Successfully fetched {} teams for season: {}", teams.size(), season);
        return teams;
    }

    public List<TeamResponse> getTeamsByName(String name) {
        logger.info("Searching for teams with name containing: {}", name);
        List<TeamResponse> teams = teamRepository
                .findAllByNameContainingIgnoreCase(name).stream()
                .map(teamConverter::teamResponseConverter)
                .collect(Collectors.toList());
        logger.info("Found {} teams with name containing: {}", teams.size(), name);
        return teams;
    }
}