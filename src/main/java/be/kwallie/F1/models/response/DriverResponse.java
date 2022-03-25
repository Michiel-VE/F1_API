package be.kwallie.F1.models.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String permanentNumber;
    private String code;
    private Date birthday;
    private String picture;
    private String country;
    private String wins;
    private double points;
    private int penaltyPoints;
}
