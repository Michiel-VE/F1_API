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

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "race_start_date", nullable = false)
    private LocalDate raceStartDate;

    @Column(name = "race_end_date", nullable = false)
    private LocalDate raceEndDate;

    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private RaceStatus status;

    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RaceSeason> raceSeasons = new ArrayList<>();
    
    /**
     * Logic to ensure status is handled before saving.
     * Note: In a real app, it is better to set the status 
     * in the Service layer by querying the RaceStatusRepository 
     * for the "SCHEDULED" entity.
     */
}