package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.response.ErrorResponse;
import be.michielve.f1_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints related to the logged-in user profile")
public class ProfileController {

    private final UserService userService;

    @Operation(
            summary = "Get profile of the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error: Authenticated user not found in database or email extraction failed")
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Authentication required",
                            HttpStatus.UNAUTHORIZED.value(),
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    ));
        }

        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Email/Username not found in authentication principal.");
        }

        String finalEmail = email;
        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok((User) user))
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Authenticated user profile not found in database for email: " + finalEmail
                        )
                );
    }
}