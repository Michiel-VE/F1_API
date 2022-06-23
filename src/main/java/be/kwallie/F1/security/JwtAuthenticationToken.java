package be.kwallie.F1.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 8234772425186775303L;
    private final String token;
    private final Claims claims;

    public JwtAuthenticationToken(String token, Claims claims) {
        super(extract(claims));
        this.claims = claims;
        this.token = token;
    }

    @Override
    public String getCredentials() {
        return this.token;
    }

    @Override
    public Claims getPrincipal() {
        return this.claims;
    }

    @Override
    public String getName() {
        return this.claims.containsKey("username") ?
                this.claims.get("username", String.class) :
                this.claims.getSubject();
    }

    public UUID getId() {
        return UUID.fromString(this.claims.getId());
    }

    private static Collection<GrantedAuthority> extract(Claims claims) {
        if (!claims.containsKey("roles")) return Collections.emptyList();
        Collection<String> roles = claims.get("roles", Collection.class);
        return roles
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
