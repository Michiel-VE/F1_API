package be.kwallie.F1.controllers;

import be.kwallie.F1.models.response.UserResponse;
import be.kwallie.F1.security.util.JwtUtil;
import be.kwallie.F1.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<UserResponse> user(Authentication authentication) {
        UUID id = jwtUtil.getIdFromAuth(authentication);
        return ResponseEntity.status(HttpStatus.OK).body(userService.getOne(id));
    }
}
