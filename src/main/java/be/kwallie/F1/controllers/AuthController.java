package be.kwallie.F1.controllers;

import be.kwallie.F1.models.User;
import be.kwallie.F1.models.request.AuthCredentialsRequest;
import be.kwallie.F1.models.response.AuthTokenResponse;
import be.kwallie.F1.security.util.JwtUtil;
import be.kwallie.F1.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody AuthCredentialsRequest request) throws AuthenticationException {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            User user = (User) this.userService.loadUserByUsername(authentication.getName());
            AuthTokenResponse token = new AuthTokenResponse();
            token.setToken(jwtUtil.generateToken(user));

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, token.getToken())
                    .body(token);
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
