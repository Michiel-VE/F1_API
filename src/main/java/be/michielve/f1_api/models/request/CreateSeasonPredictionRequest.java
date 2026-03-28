package be.michielve.f1_api.models.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeasonPredictionRequest {
    @NotEmpty(message = "Predicted teams list cannot be empty")
    @NotNull(message = "Predicted teams list is required")
    private List<UUID> predictedTeams;

}
