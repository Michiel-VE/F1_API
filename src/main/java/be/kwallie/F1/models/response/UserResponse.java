package be.kwallie.F1.models.response;

import be.kwallie.F1.models.Role;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private List<Role> roles;

}
