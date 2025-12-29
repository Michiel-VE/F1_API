package be.michielve.f1_api.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "season", schema = "f1_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Season extends BaseEntry {

    @Column(name = "season_name", nullable = false, unique = true)
    private String seasonName;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverTeamSeason> driverTeamSeasons;
}
