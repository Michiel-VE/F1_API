package be.michielve.f1_api.models.response;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamSeasonResponse {
    private String teamName;
    private String shortName;
    private String seasonName;
    private Integer points;
    private Timestamp created_at;
    private Timestamp updated_at;
}
