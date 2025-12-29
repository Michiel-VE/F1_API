package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.DriverCareerHistoryResponse;
import be.michielve.f1_api.models.response.DriverResponse;
import be.michielve.f1_api.models.response.DriverWithSeasonsResponse;
import be.michielve.f1_api.services.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "Operations related to F1 Drivers, including searching by season, permanent number, and listing all drivers.")
public class DriverController {

    private final DriverService driverService;

    @Operation(
            summary = "Get all F1 drivers",
            description = "Retrieves a list of all drivers in the database, including the seasons they participated in.",
            tags = {"Driver Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the list of drivers.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DriverWithSeasonsResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping("drivers")
    public ResponseEntity<List<DriverWithSeasonsResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @Operation(
            summary = "Get drivers for a specific season",
            description = "Fetches a list of drivers who participated in the specified season. The response will be 204 No Content if no drivers are found for that season.",
            tags = {"Driver Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved drivers for the season.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DriverResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No drivers found for the specified season. The response body will be empty."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping("drivers/{season}")
    public ResponseEntity<List<DriverWithSeasonsResponse>> getAllDriversForSeason(
            @Parameter(
                    name = "season",
                    description = "The four-digit year of the F1 season (e.g., 2020, 2021).",
                    required = true,
                    example = "2021"
            )
            @PathVariable String season
    ) {
        List<DriverWithSeasonsResponse> drivers = driverService.getAllDriversForSeason(season);

        if (drivers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(drivers);
    }

    @Operation(
            summary = "Get driver details by permanent number",
            description = "Fetches the details of a specific driver using their permanent racing number. Returns 404 Not Found if no driver is associated with the given number.",
            tags = {"Driver Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved driver details.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DriverResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver not found for the given permanent number. The response body will be empty."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping("driver")
    public ResponseEntity<DriverResponse> getDriverDetails(
            @Parameter(
                    name = "permanentNumber",
                    description = "The unique permanent racing number of the driver.",
                    required = true,
                    example = "33"
            )
            @RequestParam("permanentNumber") int permanentNumber
    ) {
        DriverResponse driver = driverService.getDriverDetails(permanentNumber);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(driver);
    }

    @Operation(
            summary = "Get driver's career history",
            description = "Fetches a list of all teams and seasons a driver has participated in.",
            tags = {"Driver Management"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the driver's career history.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DriverCareerHistoryResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping("drivers/{name}/career-history")
    public ResponseEntity<List<DriverCareerHistoryResponse>> getDriverCareerHistory(
            @Parameter(name = "name", description = "The lastname of the driver.", required = true, example = "Hamilton")
            @PathVariable String name
    ) {
        List<DriverCareerHistoryResponse> careerHistory = driverService.getDriverCareerHistory(name);
        if (careerHistory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(careerHistory);
    }


    // TODO: Create drivers/name te get history of the driver
}
