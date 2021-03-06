package be.kwallie.F1.models.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private  String origin;
    private  String picture;
}
