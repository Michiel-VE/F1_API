package be.michielve.f1_api.models.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PoolDetailsResponse {
    private UUID poolId;
    private String poolName;
    private UUID creatorId;
    private List<MemberPredictionDTO> leaderBoard;
}