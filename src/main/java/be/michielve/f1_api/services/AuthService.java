package be.michielve.f1_api.services;

import be.michielve.f1_api.enums.Role;
import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.request.LoginRequest;
import be.michielve.f1_api.models.request.RegisterRequest;
import be.michielve.f1_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Lazy
    private final AuthenticationManager authenticationManager;

    public Optional<User> findOrCreateUser(String provider, String providerId, Map<String, Object> attributes) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existingUser.isPresent()) {
            return existingUser;
        }
        User newUser = buildUserFromOAuth(provider, providerId, attributes);
        userRepository.save(newUser);
        return Optional.of(newUser);
    }

    public void registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER);
        newUser.setCreated_at(Timestamp.from(Instant.now()));
        userRepository.save(newUser);
    }

    public String loginAndGetCookieHeader(LoginRequest request, boolean isProduction, long expirationMs) {
        UserDetails user = (UserDetails) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()))
                .getPrincipal();

        String token = jwtService.generateToken(user.getUsername());
        return buildCookieHeader(token, isProduction, (int) (expirationMs / 1000));
    }

    public static String buildCookieHeader(String token, boolean isProduction, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .domain(isProduction ? "michielve.be" : null)
                .build();
        return cookie.toString();
    }

    private User buildUserFromOAuth(String provider, String providerId, Map<String, Object> attributes) {
        User user = new User();
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmail((String) attributes.get("email"));
        user.setName((String) attributes.get("name"));
        user.setRole(Role.USER);
        user.setCreated_at(Timestamp.from(Instant.now()));
        return user;
    }
}