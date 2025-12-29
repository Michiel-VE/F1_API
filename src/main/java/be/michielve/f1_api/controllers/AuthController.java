package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.request.LoginRequest;
import be.michielve.f1_api.models.request.RegisterRequest;
import be.michielve.f1_api.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login.")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with an email and password."
    )
    @ApiResponse(responseCode = "200", description = "User successfully registered.")
    @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failed.")
    @ApiResponse(responseCode = "409", description = "User with the given email already exists.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok("Registration successful");
    }

    @Operation(
            summary = "Authenticate a user",
            description = "Authenticates a user with email and password and returns a JWT."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Authentication successful, returns a JWT token.",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(responseCode = "401", description = "Invalid credentials.")
    @ApiResponse(responseCode = "400", description = "Invalid request payload.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
