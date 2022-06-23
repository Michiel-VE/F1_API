package be.kwallie.F1.services;

import be.kwallie.F1.convertors.UserJsonConvertor;
import be.kwallie.F1.error.EntityNotFoundException;
import be.kwallie.F1.models.User;
import be.kwallie.F1.models.response.UserResponse;
import be.kwallie.F1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service(value = "userService")
public class UserImpl implements UserService {
    private final UserRepository userRepository;
    private final UserJsonConvertor userJsonConvertor;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("No user found with username '%s'", username)));
    }

    @Override
    public UserResponse getOne(UUID id) {
        User user = getOneRaw(id);
        return userJsonConvertor.toResponse(user);
    }

    @Override
    public User getOneRaw(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, id));
    }
}
