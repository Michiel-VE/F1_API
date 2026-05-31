package be.michielve.f1_api.models.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePoolRequest {
    @NotBlank(message = "Pool name is required")
    private String name;
}