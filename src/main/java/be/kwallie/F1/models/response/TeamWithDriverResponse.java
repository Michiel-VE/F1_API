package be.kwallie.F1.models.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class TeamWithDriverResponse {
    private Long teamId;
    private String name;
    private  String origin;
    private List<DriverResponse> drivers;
}
