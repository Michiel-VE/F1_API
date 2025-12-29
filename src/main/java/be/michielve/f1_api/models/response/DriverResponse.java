package be.michielve.f1_api.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {
    private UUID driverId;
    private String firstname;
    private String lastname;
    private Integer permanentNumber;
    private String teamName;
    private LocalDate birthday;
    private String country;
    private String countryCode;
    private Timestamp created_at;
    private Timestamp updated_at;
}
