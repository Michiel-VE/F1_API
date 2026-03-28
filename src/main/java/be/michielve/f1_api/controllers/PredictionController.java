package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.request.CreatePredictionRequest;
import be.michielve.f1_api.models.request.CreateSeasonPredictionRequest;
import be.michielve.f1_api.models.response.ErrorResponse;
import be.michielve.f1_api.services.PredictionService;
import be.michielve.f1_api.services.UserService;

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
public class PredictionController {

    private final UserService userService;
    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<Object> createPrediction(
            @Valid @RequestBody CreatePredictionRequest request,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            predictionService.createPrediction(user.getId(), request);
            
            return buildSuccessResponse("Prediction Created", HttpStatus.CREATED);
        } catch (UnauthorizedException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/team")
    public ResponseEntity<Object> createTeamPrediction(
            @Valid @RequestBody CreateSeasonPredictionRequest request,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            predictionService.createTeamPrediction(user.getId(), request);
            
            return buildSuccessResponse("Team Prediction Created", HttpStatus.CREATED);
        } catch (UnauthorizedException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private ResponseEntity<Object> buildSuccessResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "message", message,
                "statusCode", status.value(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    private ResponseEntity<Object> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                message,
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ));
    }

    private static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}