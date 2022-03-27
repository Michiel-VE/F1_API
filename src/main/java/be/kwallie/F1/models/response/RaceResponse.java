package be.kwallie.F1.models.response;

import lombok.*;

import java.util.Date;

@Data
@Builder
public class RaceResponse {
    private Long id;
    private String name;
    private double lat;
    private double lon;
    private String city;
    private String country;
    private Date date;
    private String timezone;
    private boolean active;
}
