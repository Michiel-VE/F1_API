package be.michielve.f1_api.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team", schema = "f1_api")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Team extends BaseEntry {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String shortName;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "base", nullable = false)
    private String base;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverTeamSeason> driverTeamSeasons;
}

