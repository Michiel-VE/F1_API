package be.kwallie.F1.models.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {
    protected long id;
    private String firstName;
    private String lastName;
    private String permanentNumber;
    private String code;
    private String picture;
    private String country;
    private String wins;
    private double points;
    private int penaltyPoints;
}
