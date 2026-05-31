package be.michielve.f1_api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "team", schema = "f1_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
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
    @ToString.Exclude
    @JsonIgnoreProperties("team")
    private List<DriverTeamSeason> driverTeamSeasons;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team team)) return false;
        return getId() != null && getId().equals(team.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}