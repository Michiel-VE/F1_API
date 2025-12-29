package be.michielve.f1_api.models.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverWithSeasonsResponse {
    private UUID driverId;
    private String firstname;
    private String lastname;
    private Integer permanentNumber;
    private LocalDate birthday;
    private String country;
    private String countryCode;
    private List<TeamSeasonResponse> teamSeasons;
}
