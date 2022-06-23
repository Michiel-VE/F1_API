package be.kwallie.F1.services;

import be.kwallie.F1.models.User;
import be.kwallie.F1.models.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

public interface UserService {
    UserResponse getOne (UUID id);
    User getOneRaw(UUID id);

    UserDetails loadUserByUsername(String var1) throws UsernameNotFoundException;
}
