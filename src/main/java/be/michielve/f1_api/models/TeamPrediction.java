package be.michielve.f1_api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team_predictions", schema = "f1_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TeamPrediction extends BaseEntry {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "members", "creator"})
    private Pool pool;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_prediction_teams",
        schema = "f1_api",
        joinColumns = @JoinColumn(name = "prediction_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    @OrderColumn(name = "prediction_order")
    @Builder.Default
    @JsonIgnoreProperties("driverTeamSeasons")
    private List<Team> predictedTeams = new ArrayList<>();
}