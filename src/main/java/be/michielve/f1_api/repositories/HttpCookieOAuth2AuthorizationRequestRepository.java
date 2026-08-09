package be.michielve.f1_api.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final ObjectMapper objectMapper;

    public HttpCookieOAuth2AuthorizationRequestRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        boolean isProduction = System.getenv("BASE_URL") != null;
        String secret = serialize(authorizationRequest);

        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, secret, COOKIE_EXPIRE_SECONDS, isProduction);

        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS, isProduction);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
            HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean isProduction) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        boolean isProduction = System.getenv("BASE_URL") != null;
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction);
        response.addCookie(cookie);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            Map<String, Object> data = Map.of(
                    "clientId", authorizationRequest.getClientId(),
                    "authorizationUri", authorizationRequest.getAuthorizationUri(),
                    "redirectUri", authorizationRequest.getRedirectUri() != null ? authorizationRequest.getRedirectUri() : "",
                    "scopes", authorizationRequest.getScopes(),
                    "state", authorizationRequest.getState() != null ? authorizationRequest.getState() : "",
                    "additionalParameters", authorizationRequest.getAdditionalParameters(),
                    "attributes", authorizationRequest.getAttributes());
            byte[] bytes = objectMapper.writeValueAsBytes(data);
            return Base64.getUrlEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(bytes, Map.class);

            @SuppressWarnings("unchecked")
            Set<String> scopes = data.get("scopes") instanceof Set ? (Set<String>) data.get("scopes")
                    : new java.util.LinkedHashSet<>((java.util.List<String>) data.get("scopes"));

            @SuppressWarnings("unchecked")
            Map<String, Object> additionalParameters = (Map<String, Object>) data.get("additionalParameters");

            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

            return OAuth2AuthorizationRequest.authorizationCode()
                    .clientId((String) data.get("clientId"))
                    .authorizationUri((String) data.get("authorizationUri"))
                    .redirectUri((String) data.get("redirectUri"))
                    .scopes(scopes)
                    .state((String) data.get("state"))
                    .additionalParameters(additionalParameters)
                    .attributes(attributes)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize OAuth2AuthorizationRequest", e);
        }
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }
}