package be.kwallie.F1.models.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TeamWithDriverResponse {
    private Long driverId;
    private String firstName;
    private String lastName;
    private String permanentNumber;
    private String code;
    private Date birthday;
    private String picture;
    private String country;
    private String countryCode;
    private String wins;
    private double points;
    private int penaltyPoints;
    private Long teamId;
    private String name;
    private  String origin;
}
