package be.kwallie.F1.controllers;

import be.kwallie.F1.models.response.TeamResponse;
import be.kwallie.F1.models.response.TeamWithDriverResponse;
import be.kwallie.F1.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/teams")
    public ResponseEntity<List<TeamWithDriverResponse>> getAllTeamsWithDrivers() {
        return ResponseEntity.ok(teamService.getAllTeamsWithDrivers());
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeam(id));
    }
}
