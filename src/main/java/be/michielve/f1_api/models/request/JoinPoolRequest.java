package be.michielve.f1_api.models.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinPoolRequest {
    @NotBlank(message = "Invite code is required")
    private String inviteCode;
}