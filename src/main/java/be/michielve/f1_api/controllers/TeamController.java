package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.TeamResponse;
import be.michielve.f1_api.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Operations related to F1 Teams, including listing teams by season and searching by name.")
public class TeamController {

    private final TeamService teamService;

    @Operation(
            summary = "Get all teams for a specific season",
            description = "Fetches a comprehensive list of all F1 teams that participated in the specified season. Returns 204 No Content if no teams are found for the season.",
            tags = {"Team Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved teams for the season.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TeamResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No teams found for the specified season. The response body will be empty."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during team retrieval.",
                    content = @Content
            )
    })
    @GetMapping("teams/{season}")
    public ResponseEntity<List<TeamResponse>> getAllTeamsForCurrentSeason(
            @Parameter(
                    name = "season",
                    description = "The four-digit year of the F1 season (e.g., 2020, 2021, 2022).",
                    required = true,
                    example = "2021"
            )
            @PathVariable String season
    ) {
        List<TeamResponse> teams = teamService.getAllTeamsForSeason(season);

        if (teams.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(teams);
    }

    @Operation(
            summary = "Search for F1 teams by name",
            description = "Fetches details of F1 teams whose names contain the provided string (case-insensitive). Returns 404 Not Found if no teams match the search criterion.",
            tags = {"Team Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved team details matching the name.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TeamResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No team found with the given name or matching the search string. The response body will be empty."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during team search.",
                    content = @Content
            )
    })
    @GetMapping("team/{name}")
    public ResponseEntity<List<TeamResponse>> getTeamByName(
            @Parameter(
                    name = "name",
                    description = "The full or partial name of the F1 team to search for (e.g., 'Red Bull', 'Mercedes', 'Ferrari').",
                    required = true,
                    example = "Red Bull Racing"
            )
            @PathVariable String name
    ) {
        List<TeamResponse> team = teamService.getTeamsByName(name);

        if (team.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(team);
    }
}