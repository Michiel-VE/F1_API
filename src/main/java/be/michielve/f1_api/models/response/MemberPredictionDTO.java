package be.michielve.f1_api.models.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberPredictionDTO {
    private UUID userId;
    private String username;
    private String picture;
    private List<String> predictedTeamIds;
}