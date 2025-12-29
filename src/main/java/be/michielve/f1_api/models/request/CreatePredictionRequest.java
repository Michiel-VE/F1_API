package be.michielve.f1_api.models.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreatePredictionRequest {

    @NotNull
    private UUID raceId;

    @NotNull
    @Size(min = 10, max = 10, message = "You must provide exactly 10 driver IDs.")
    private List<UUID> predictedDrivers;
}
