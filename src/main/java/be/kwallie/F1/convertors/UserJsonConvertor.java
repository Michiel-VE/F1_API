package be.kwallie.F1.convertors;

import be.kwallie.F1.models.User;
import be.kwallie.F1.models.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserJsonConvertor {
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }
}
