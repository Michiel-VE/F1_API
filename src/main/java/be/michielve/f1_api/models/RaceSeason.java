package be.michielve.f1_api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "race_season", schema = "f1_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaceSeason extends BaseEntry {

    @ManyToOne
    @JoinColumn(name = "race_id")
    private Race race;

    @ManyToOne
    @JoinColumn(name = "season_id")
    private Season season;

}
