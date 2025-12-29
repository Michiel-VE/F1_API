package be.michielve.f1_api.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "driver", schema = "f1_api")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Driver extends BaseEntry {

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(name = "permanent_number", nullable = false, unique = true)
    private Integer permanentNumber;

    @Column(name = "driver_code", nullable = false, unique = true, length = 3)
    private String driverCode;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    private String country;

    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @Column(name = "team", nullable = false)
    private String team;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverTeamSeason> driverTeamSeasons;
}
