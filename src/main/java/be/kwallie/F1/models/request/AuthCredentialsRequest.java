package be.kwallie.F1.models.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthCredentialsRequest {
    private String username;
    private String password;
}
