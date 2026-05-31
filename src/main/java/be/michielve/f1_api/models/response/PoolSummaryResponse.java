package be.michielve.f1_api.models.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PoolSummaryResponse {
    private UUID id;
    private String name;
    private int memberCount;
}