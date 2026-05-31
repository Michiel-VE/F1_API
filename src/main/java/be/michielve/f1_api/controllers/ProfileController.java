package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.response.ErrorResponse;
import be.michielve.f1_api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Authentication required",
                            HttpStatus.UNAUTHORIZED.value(),
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        }

        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Could not resolve identity from authentication token.",
                            HttpStatus.UNAUTHORIZED.value(),
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        }

        return userService.findByEmail(email)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok((User) user))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "User profile not found.",
                                HttpStatus.NOT_FOUND.value(),
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }
}