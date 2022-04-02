package be.kwallie.F1.services;

import be.kwallie.F1.models.response.TeamResponse;
import be.kwallie.F1.models.response.TeamWithDriverResponse;

import java.util.List;

public interface TeamService {
    List<TeamWithDriverResponse> getAllTeamsWithDrivers();
    TeamResponse getTeam(Long id);
}
