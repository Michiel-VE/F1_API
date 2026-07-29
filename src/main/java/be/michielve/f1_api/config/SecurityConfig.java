package be.michielve.f1_api.config;

import be.michielve.f1_api.security.filters.JwtFilter;
import be.michielve.f1_api.security.handlers.OAuthSuccessHandler;
import be.michielve.f1_api.services.JwtService;
import be.michielve.f1_api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final UserService userService;
        private final JwtService jwtService;
        private final OAuthSuccessHandler oAuthSuccessHandler;
        private final be.michielve.f1_api.repositories.HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

        @Value("${spring.security.oauth2.client.registration.google.client-id}")
        private String googleClientId;

        @Value("${spring.security.oauth2.client.registration.google.client-secret}")
        private String googleClientSecret;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                request -> request.getServletPath()
                                                                                .startsWith("/api/v1/")))
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/register",
                                                                "/api/v1/auth/logout",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/v1/profile",
                                                                "/api/v1/predictions",
                                                                "/api/v1/predictions/**")
                                                .authenticated()
                                                .anyRequest().permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .baseUri("/oauth2/authorization")
                                                                .authorizationRequestRepository(
                                                                                httpCookieOAuth2AuthorizationRequestRepository))
                                                .clientRegistrationRepository(clientRegistrationRepository())
                                                .successHandler(oAuthSuccessHandler)
                                                .failureUrl(
                                                                (System.getenv("BASE_URL") != null
                                                                                ? System.getenv("BASE_URL")
                                                                                : "") + "/login?error")
                                                .redirectionEndpoint(redirection -> redirection
                                                                .baseUri("/login/oauth2/code/*")))
                                .logout(logout -> logout
                                                .logoutUrl("/api/v1/auth/logout")
                                                .addLogoutHandler((request, response, authentication) -> {
                                                        boolean isProduction = "prod".equalsIgnoreCase(
                                                                        System.getenv("SPRING_PROFILES_ACTIVE"))
                                                                        || (System.getenv("BASE_URL") != null && System
                                                                                        .getenv("BASE_URL")
                                                                                        .contains("michielve.be"));

                                                        org.springframework.http.ResponseCookie deleteCookie = org.springframework.http.ResponseCookie
                                                                        .from("jwt", "")
                                                                        .httpOnly(true)
                                                                        .secure(isProduction)
                                                                        .path("/")
                                                                        .maxAge(0)
                                                                        .sameSite("Lax")
                                                                        .domain(isProduction ? "michielve.be" : null)
                                                                        .build();
                                                        response.addHeader(
                                                                        org.springframework.http.HttpHeaders.SET_COOKIE,
                                                                        deleteCookie.toString());
                                                })
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpStatus.OK.value());
                                                }))
                                .userDetailsService(userService)
                                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
                return new InMemoryClientRegistrationRepository(googleClientRegistration());
        }

        private ClientRegistration googleClientRegistration() {
                String forcedRedirectUri = System
                                .getenv("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI");

                String redirectUri = (forcedRedirectUri != null && !forcedRedirectUri.isEmpty())
                                ? forcedRedirectUri
                                : "{baseUrl}/login/oauth2/code/{registrationId}";

                return ClientRegistration.withRegistrationId("google")
                                .clientId(googleClientId)
                                .clientSecret(googleClientSecret)
                                .scope("openid", "profile", "email")
                                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                                .tokenUri("https://oauth2.googleapis.com/token")
                                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                                .userNameAttributeName("sub")
                                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                                .clientName("Google")
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .redirectUri(redirectUri)
                                .build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                
                configuration.setAllowedOriginPatterns(
                                List.of(
                                                "http://localhost:4200",
                                                "http://localhost:8080",
                                                "https://f1.michielve.be",
                                                "https://f1-api.michielve.be"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(
                                List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
                configuration.setExposedHeaders(List.of("Authorization"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public JwtFilter jwtFilter() {
                return new JwtFilter(jwtService);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}