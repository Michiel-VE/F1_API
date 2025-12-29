package be.michielve.f1_api.services;

import be.michielve.f1_api.enums.Role;
import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.request.LoginRequest;
import be.michielve.f1_api.models.request.RegisterRequest;
import be.michielve.f1_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.secret.key}")
    private String secretKey;

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

        // Create new user
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER);
        newUser.setCreated_at(Timestamp.from(Instant.now()));

        // Save new user to database
        userRepository.save(newUser);
    }

    // Method for login: authenticates user and returns JWT token
    public String login(LoginRequest request) {
        try {
           String  decryptedPassword = decryptPassword(request.getPassword());

            UserDetails user = (UserDetails) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), decryptedPassword)
            ).getPrincipal();

            return jwtService.generateToken(user.getUsername());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User buildUserFromOAuth(String provider, String providerId, Map<String, Object> attributes) {
        User user = new User();
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmail((String) attributes.get("email"));
        user.setName((String) attributes.get("name"));
        user.setRole(Role.USER); // Default role
        user.setCreated_at(Timestamp.from(Instant.now()));
        return user;
    }

    private String decryptPassword(String encryptedPassword) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);

        SecretKeySpec secretKeyspec = new SecretKeySpec(sliceString(secretKey).getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeyspec);

        byte[] decryptedBytes = cipher.doFinal(decodedBytes);


        return new String(decryptedBytes);

    }

    public static String sliceString(String input) {
        int offset = 15;

        if (input == null || input.isEmpty()) {
            return "";
        }
        int length = input.length() / 2;

        return input.substring(offset, offset + length);
    }
}
