package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.request.LoginRequest;
import be.michielve.f1_api.models.request.RegisterRequest;
import be.michielve.f1_api.services.AuthService;
import be.michielve.f1_api.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        boolean isProduction = System.getenv("BASE_URL") != null;
        String cookieHeader = authService.loginAndGetCookieHeader(
                loginRequest, isProduction, jwtService.getExpiration());
        response.addHeader("Set-Cookie", cookieHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        boolean isProduction = System.getenv("BASE_URL") != null;
        String cookieHeader = AuthService.buildCookieHeader("", isProduction, 0);
        response.addHeader("Set-Cookie", cookieHeader);
        return ResponseEntity.ok().build();
    }
}