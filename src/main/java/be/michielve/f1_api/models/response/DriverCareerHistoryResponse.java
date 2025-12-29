package be.michielve.f1_api.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DriverCareerHistoryResponse {
    private String season;
    private String team;
    private String position;
    private String points;
}
