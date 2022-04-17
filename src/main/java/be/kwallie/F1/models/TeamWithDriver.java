package be.kwallie.F1.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class TeamWithDriver {
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
