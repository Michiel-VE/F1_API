package be.michielve.f1_api.security.handlers;

import be.michielve.f1_api.services.AuthService;
import be.michielve.f1_api.services.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AuthService authService;

    public OAuthSuccessHandler(JwtService jwtService, @Lazy AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String provider = oauthToken.getAuthorizedClientRegistrationId();
            String providerId = oauth2User.getName();

            authService.findOrCreateUser(provider, providerId, oauth2User.getAttributes())
                    .ifPresent(user -> {
                        String token = jwtService.generateToken(user.getEmail());

                        // Build the redirect URL to your Angular app
                        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/login")
                                .queryParam("token", token)
                                .build().toUriString();

                        try {
                            // Redirect the browser instead of writing JSON to the body
                            response.sendRedirect(targetUrl);
                        } catch (IOException e) {
                            log.error("Failed to redirect after OAuth2 success", e);
                        }
                    });
        }
    }
}