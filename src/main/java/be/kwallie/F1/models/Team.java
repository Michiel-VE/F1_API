package be.kwallie.F1.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "team_id")
    @JsonManagedReference
    private List<Driver> drivers = new ArrayList<>();

    @NotBlank
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ORIGIN", nullable = false)
    private String origin;

    @Column(name = "PICTURE", nullable = false)
    private String picture;
}
