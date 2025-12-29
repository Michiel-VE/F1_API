package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.Prediction;
import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.request.CreatePredictionRequest;
import be.michielve.f1_api.models.response.ErrorResponse;
import be.michielve.f1_api.services.PredictionService;
import be.michielve.f1_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(name = "Predictions", description = "Endpoints for creating and managing F1 race predictions")
public class PredictionController {

    private final UserService userService;
    private final PredictionService predictionService;

    @Operation(
            summary = "Create a prediction for a race",
            description = "Allows a logged-in user to create a prediction for the next F1 race. " +
                    "The user must provide exactly 10 driver IDs in the predicted order.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Prediction created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Prediction.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or prediction already exists",
                            content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or Race not found",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<Object> createPrediction(
            @Valid @RequestBody CreatePredictionRequest request,
            Authentication authentication
    ) {
        try {

            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Authentication required", HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            }

            String userEmail = authentication.getName();

            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Prediction prediction = predictionService.createPrediction(user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Prediction Created",
                            "statusCode", HttpStatus.CREATED.value(),
                            "timestamp", java.time.LocalDateTime.now().toString()
                    ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
