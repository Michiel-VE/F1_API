package be.michielve.f1_api.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "race", schema = "f1_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Race extends BaseEntry {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "country", nullable = false, unique = true)
    private String country;

    @Column(name = "race_start_date", nullable = false, unique = true)
    private LocalDate raceStartDate;

    @Column(name = "race_end_date", nullable = false, unique = true)
    private LocalDate raceEndDate;

    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RaceSeason> raceSeasons = new ArrayList<>();
}
