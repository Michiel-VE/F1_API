package be.kwallie.F1.error;

import org.springframework.security.core.Authentication;

public class AuthenticationNotSupportedException extends RuntimeException {
    public AuthenticationNotSupportedException(Class<? extends Authentication> authentication) {
        super(String.format("Authentication of type '%s' is not supported", authentication.getName()));
    }
}
