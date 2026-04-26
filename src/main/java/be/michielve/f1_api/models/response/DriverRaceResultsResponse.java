package be.michielve.f1_api.models.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DriverRaceResultsResponse {
    private String firstname;
    private String lastname;
    private String season;
    private List<RaceResultDTO> results;
}