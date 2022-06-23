package be.kwallie.F1.security.util;

import be.kwallie.F1.error.AuthenticationNotSupportedException;
import be.kwallie.F1.models.Role;
import be.kwallie.F1.models.User;
import be.kwallie.F1.security.JwtAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class JwtUtil implements Serializable {
    private final SecretKey secret;

    private static final long serialVersionUID = -2550185165626007488L;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public UUID getIdFromAuth(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) auth).getId();
        } else {
            throw new AuthenticationNotSupportedException(auth.getClass());
        }
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean ignoreTokenExpiration(String token) {
        return false;
    }

    public String generateToken(User user) {
        Claims claims = Jwts.claims();

        claims.setSubject(user.getId().toString());
        claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        claims.put("username", user.getUsername());
        return doGenerateToken(claims, user);
    }

    private String doGenerateToken(Map<String, Object> claims, User user) {
        return Jwts.builder()
                .setClaims(claims)
                .setId(String.valueOf(user.getId()))
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(secret)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token) {
        return (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public JwtAuthenticationToken getAuthentication(String header) {
        String token = header.substring("Bearer ".length());

        if (!isValid(token)) return null;

        Claims claims = decode(token);
        JwtAuthenticationToken auth = new JwtAuthenticationToken(token, claims);
        auth.setAuthenticated(true);
        return auth;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isValid(String token) {
        if (StringUtils.isBlank(token)) return false;

        // verify the token content
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims decode(String token) {
        if (StringUtils.isBlank(token)) return null;

        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
