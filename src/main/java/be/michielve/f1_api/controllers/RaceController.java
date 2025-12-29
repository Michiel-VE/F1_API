package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.RaceResponse;
import be.michielve.f1_api.services.RaceService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "Race Management", description = "Operations related to F1 Races, including listing races by season.")
public class RaceController {

    private final RaceService raceService;

    @Operation(
            summary = "Get all races for a specific season",
            description = "Fetches a comprehensive list of all F1 races that took place in the specified season. Returns 204 No Content if no races are found for the season.",
            tags = {"Race Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved races for the season.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = RaceResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No races found for the specified season. The response body will be empty."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during race retrieval.",
                    content = @Content
            )
    })
    @GetMapping("races/{season}")
    public ResponseEntity<List<RaceResponse>> getAllRacesForSeason(
            @Parameter(
                    name = "season",
                    description = "The four-digit year of the F1 season (e.g., 2020, 2021, 2022).",
                    required = true,
                    example = "2021"
            )
            @PathVariable String season
    ) {
        List<RaceResponse> races = raceService.getAllRacesForSeason(season);

        if (races.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(races);
    }
}
