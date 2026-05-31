package be.michielve.f1_api.models.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionStatusResponse {
    private boolean hasPools;
    private boolean hasPersonalPrediction;
}