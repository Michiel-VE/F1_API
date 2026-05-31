package be.michielve.f1_api.models.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SavedPredictionResponse {
    private List<String> predictedTeams;
}