package be.michielve.f1_api.security.handlers;

import be.michielve.f1_api.repositories.HttpCookieOAuth2AuthorizationRequestRepository;
import be.michielve.f1_api.services.AuthService;
import be.michielve.f1_api.services.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    public OAuthSuccessHandler(JwtService jwtService,
            @Lazy AuthService authService,
            HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String provider = oauthToken.getAuthorizedClientRegistrationId();
            String providerId = oauth2User.getName();

            String envFrontendUrl = System.getenv("FRONTEND_URL");
            String baseUrl = (envFrontendUrl != null && !envFrontendUrl.isEmpty())
                    ? envFrontendUrl
                    : "http://localhost:4200";

            String cookieReturnUrl = httpCookieOAuth2AuthorizationRequestRepository
                    .getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                    .map(Cookie::getValue)
                    .orElse("/");

            final String finalReturnUrl = (cookieReturnUrl.startsWith("/") && !cookieReturnUrl.contains(" "))
                    ? cookieReturnUrl
                    : "/";

            httpCookieOAuth2AuthorizationRequestRepository.deleteCookie(request, response,
                    HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME);

            authService.findOrCreateUser(provider, providerId, oauth2User.getAttributes())
                    .ifPresentOrElse(user -> {
                        String token = jwtService.generateToken(user.getEmail());

                        String targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                                .path("/login")
                                .queryParam("token", token)
                                .queryParam("returnUrl", finalReturnUrl)
                                .build().toUriString();

                        try {
                            log.info("Redirecting to frontend: {}", targetUrl);
                            response.sendRedirect(targetUrl);
                        } catch (IOException e) {
                            log.error("Failed to redirect after OAuth2 success", e);
                        }
                    }, () -> {
                        log.error("OAuth success but user could not be found or created in database");
                        try {
                            response.sendRedirect(baseUrl + "/login?error=user_creation_failed");
                        } catch (IOException e) {
                            log.error("Failed to redirect to error page", e);
                        }
                    });
        }
    }
}