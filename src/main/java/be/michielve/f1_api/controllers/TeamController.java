package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.TeamResponse;
import be.michielve.f1_api.services.TeamService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class TeamController {

        private final TeamService teamService;

        @GetMapping("teams/{season}")
        public ResponseEntity<List<TeamResponse>> getAllTeamsForCurrentSeason(
                        @PathVariable String season) {
                List<TeamResponse> teams = teamService.getAllTeamsForSeason(season);

                if (teams.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(teams);
        }

        @GetMapping("team/{name}")
        public ResponseEntity<List<TeamResponse>> getTeamByName(
                        @PathVariable String name) {
                List<TeamResponse> team = teamService.getTeamsByName(name);

                if (team.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok(team);
        }
}