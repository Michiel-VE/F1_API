package be.michielve.f1_api.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {
    private UUID id;
    private String name;
    private String country;
    private String shortName;
    private String base;
    private Timestamp created_at;
    private Timestamp updated_at;
}
