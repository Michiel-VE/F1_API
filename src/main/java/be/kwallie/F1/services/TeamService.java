package be.kwallie.F1.services;

import be.kwallie.F1.models.response.TeamResponse;

import java.util.List;

public interface TeamService {
    List<TeamResponse> getAllTeams();
    TeamResponse getTeam(Long id);
}
