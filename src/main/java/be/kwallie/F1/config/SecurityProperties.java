package be.kwallie.F1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "be.kwallie.security")
@Data
public class SecurityProperties {
    /* The string value of a secret key used for the HMAC signature */
    private String secret;

    /* The name of the JSON web token issuer */
    private String issuer;

    /* The time in hours it takes for an access token to expire */
    private int accessTokenExpirationDuration;
}