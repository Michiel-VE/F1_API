package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.Pool;
import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.request.CreatePoolRequest;
import be.michielve.f1_api.models.request.CreatePredictionRequest;
import be.michielve.f1_api.models.request.CreateSeasonPredictionRequest;
import be.michielve.f1_api.models.request.JoinPoolRequest;
import be.michielve.f1_api.models.response.ErrorResponse;
import be.michielve.f1_api.models.response.PoolDetailsResponse;
import be.michielve.f1_api.models.response.PoolSummaryResponse;
import be.michielve.f1_api.models.response.PredictionStatusResponse;
import be.michielve.f1_api.models.response.SavedPredictionResponse;
import be.michielve.f1_api.services.PredictionService;
import be.michielve.f1_api.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        } catch (AccessDeniedException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pools")
    public ResponseEntity<List<PoolSummaryResponse>> getUserPools(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<PoolSummaryResponse> pools = predictionService.getUserPools(user.getId());
        return ResponseEntity.ok(pools);
    }

    @GetMapping("/pools/{poolId}")
    public ResponseEntity<Object> getPoolDetails(
            @PathVariable UUID poolId,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            PoolDetailsResponse details = predictionService.getPoolDetails(user.getId(), poolId);
            return ResponseEntity.ok(details);
        } catch (AccessDeniedException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<PredictionStatusResponse> getPredictionStatus(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        PredictionStatusResponse status = predictionService.getPredictionStatus(user.getId());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/team/personal")
    public ResponseEntity<SavedPredictionResponse> getPersonalTeamPrediction(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        SavedPredictionResponse prediction = predictionService.getPersonalTeamPrediction(user.getId());
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/team/pool/{poolId}")
    public ResponseEntity<SavedPredictionResponse> getPoolTeamPrediction(
            @PathVariable UUID poolId,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        SavedPredictionResponse prediction = predictionService.getPoolTeamPrediction(user.getId(), poolId);
        return ResponseEntity.ok(prediction);
    }

    @PostMapping("/pools")
    public ResponseEntity<Object> createPool(
            @Valid @RequestBody CreatePoolRequest request,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            Pool createdPool = predictionService.createPool(user.getId(), request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Pool Created Successfully",
                    "poolId", createdPool.getId(),
                    "inviteCode", createdPool.getInviteCode()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pools/join")
    public ResponseEntity<Object> joinPool(
            @Valid @RequestBody JoinPoolRequest request,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            predictionService.joinPool(user.getId(), request);

            return buildSuccessResponse("Successfully joined the pool", HttpStatus.OK);
        } catch (IllegalStateException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/pools/{poolId}/leave")
    public ResponseEntity<Object> leavePool(
            @PathVariable UUID poolId,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            predictionService.leaveOrDeletePool(user.getId(), poolId);
            return buildSuccessResponse("Successfully left or dissolved the pool", HttpStatus.OK);
        } catch (IllegalStateException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/pools/{poolId}/members/{memberId}")
    public ResponseEntity<Object> kickMember(
            @PathVariable UUID poolId,
            @PathVariable UUID memberId,
            Authentication authentication) {
        try {
            User admin = getAuthenticatedUser(authentication);
            predictionService.kickMemberOrDeletePool(admin.getId(), poolId, memberId);
            return buildSuccessResponse("Member removed or pool dissolved successfully", HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleNotFound(IllegalArgumentException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleForbidden(AccessDeniedException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
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
                "timestamp", LocalDateTime.now().toString()));
    }

    private ResponseEntity<Object> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                message,
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

    private static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}